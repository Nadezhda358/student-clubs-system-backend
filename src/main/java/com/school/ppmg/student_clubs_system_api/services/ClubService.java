package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.*;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMedia;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacher;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacherId;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.MediaType;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.exceptions.ConflictException;
import com.school.ppmg.student_clubs_system_api.exceptions.ResourceNotFoundException;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMediaRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubTeacherRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubTeacherRepository clubTeacherRepository;
    private final ClubMediaRepository clubMediaRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public Page<ClubListDto> getAll(Boolean active, Pageable pageable) {
        Boolean effectiveActive = canViewInactiveClubs() ? active : Boolean.TRUE;

        Page<Club> page = effectiveActive == null
                ? clubRepository.findAll(pageable)
                : clubRepository.findAllByIsActive(effectiveActive, pageable);

        return page.map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public Page<ClubListDto> getManagedClubs(Boolean active, Pageable pageable) {
        User teacher = getCurrentTeacher();

        Page<Club> page = active == null
                ? clubRepository.findDistinctByTeachers_Teacher_Id(teacher.getId(), pageable)
                : clubRepository.findDistinctByTeachers_Teacher_IdAndIsActive(teacher.getId(), active, pageable);

        return page.map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public ClubDto getById(Long id) {
        Club club = getClubOrThrow(id);

        if (!Boolean.TRUE.equals(club.getIsActive()) && !canViewInactiveClubs()) {
            throw new ResourceNotFoundException("Club with id=" + id + " not found");
        }

        return toDto(club);
    }

    @Transactional(readOnly = true)
    public ClubDto getManagedById(Long id) {
        return toDto(getManagedClubOrThrow(id));
    }

    @Transactional
    public ClubDto create(CreateClubDto dto) {
        return create(dto, new CreateClubOptions(null, List.of()));
    }

    @Transactional
    public ClubDto create(CreateClubDto dto, CreateClubOptions options) {
        if (clubRepository.existsByName(dto.name())) {
            throw new ConflictException("Club name already exists: " + dto.name());
        }

        CreateClubOptions effectiveOptions = options == null
                ? new CreateClubOptions(null, List.of())
                : options;

        Club club = new Club();
        applyUpsert(club, dto);
        club.setCreatedBy(authService.getCurrentUser());
        clubRepository.saveAndFlush(club);

        List<String> uploadedUrls = new ArrayList<>();
        try {
            attachTeachers(club, dto.teacherIds());
            uploadMainImage(club, effectiveOptions.mainImage(), uploadedUrls);
            saveClubMedia(club, effectiveOptions.mediaFiles(), uploadedUrls);

            return toDto(clubRepository.save(club));
        } catch (RuntimeException ex) {
            cleanupUploadedFiles(uploadedUrls);
            throw ex;
        }
    }

    @Transactional
    public ClubDto update(Long id, UpsertClubDto dto) {
        return updateClub(getClubOrThrow(id), dto);
    }

    @Transactional
    public ClubDto updateManagedClub(Long id, ManageClubDto dto) {
        return updateClub(getManagedClubOrThrow(id), dto);
    }

    @Transactional
    public void delete(Long id) {
        clubRepository.delete(getClubOrThrow(id));
    }

    @Transactional
    public void addTeachers(Long clubId, List<Long> teacherIds) {
        Club club = getClubOrThrow(clubId);

        for (Long teacherId : normalizeTeacherIds(teacherIds)) {
            User teacher = getTeacherOrThrow(teacherId);

            if (clubTeacherRepository.existsByClub_IdAndTeacher_Id(clubId, teacherId)) {
                continue;
            }

            if (clubTeacherRepository.countAllByClubIdAndTeacherId(clubId, teacherId) > 0) {
                clubTeacherRepository.restoreByClubIdAndTeacherId(clubId, teacherId);
                continue;
            }

            createTeacherRelation(club, teacher);
        }
    }

    @Transactional
    public void removeTeacher(Long clubId, Long teacherId) {
        getClubOrThrow(clubId);
        if (teacherId == null) {
            return;
        }

        clubTeacherRepository.findByClub_IdAndTeacher_Id(clubId, teacherId)
                .ifPresent(clubTeacherRepository::delete);
    }

    @Transactional
    public ClubDto updateMainImage(Long id, MultipartFile file) {
        return updateClubMainImage(getClubOrThrow(id), file);
    }

    @Transactional
    public ClubDto updateManagedMainImage(Long id, MultipartFile file) {
        return updateClubMainImage(getManagedClubOrThrow(id), file);
    }

    private ClubDto updateClub(Club club, ClubWriteRequest dto) {
        ensureNameIsAvailable(dto.name(), club.getId());
        applyUpsert(club, dto);
        return toDto(clubRepository.save(club));
    }

    private ClubDto updateClubMainImage(Club club, MultipartFile file) {
        String url = s3StorageService.upload(file, "clubs/" + club.getId() + "/main-image");
        club.setMainImageUrl(url);
        return toDto(clubRepository.save(club));
    }

    private Club getClubOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id=" + id + " not found"));
    }

    private Club getManagedClubOrThrow(Long id) {
        Club club = getClubOrThrow(id);
        User teacher = getCurrentTeacher();

        if (!clubTeacherRepository.existsByClub_IdAndTeacher_Id(id, teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not manage this club");
        }

        return club;
    }

    private User getCurrentTeacher() {
        User currentUser = authService.getCurrentUser();

        if (currentUser.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher access required");
        }

        return currentUser;
    }

    private void ensureNameIsAvailable(String name, Long clubId) {
        if (clubRepository.existsByNameAndIdNot(name, clubId)) {
            throw new ConflictException("Club name already exists: " + name);
        }
    }

    private void applyUpsert(Club club, ClubWriteRequest dto) {
        club.setName(dto.name());
        club.setDescription(dto.description());
        club.setScheduleText(dto.scheduleText());
        club.setRoom(dto.room());
        club.setContactEmail(dto.contactEmail());
        club.setContactPhone(dto.contactPhone());
        club.setIsActive(dto.isActive());
    }

    private ClubListDto toListDto(Club club) {
        return new ClubListDto(
                club.getId(),
                club.getName(),
                club.getRoom(),
                club.getIsActive(),
                club.getMainImageUrl()
        );
    }

    private ClubDto toDto(Club club) {
        List<TeacherDto> teachers = club.getTeachers() == null ? List.of()
                : club.getTeachers().stream()
                .sorted(Comparator
                        .comparing((ClubTeacher clubTeacher) -> clubTeacher.getTeacher().getFirstName())
                        .thenComparing(clubTeacher -> clubTeacher.getTeacher().getLastName())
                        .thenComparing(clubTeacher -> clubTeacher.getTeacher().getId()))
                .map(teacher -> new TeacherDto(
                        teacher.getTeacher().getId(),
                        teacher.getTeacher().getFirstName() + " " + teacher.getTeacher().getLastName()
                ))
                .toList();

        List<MediaDto> media = club.getMedia() == null ? List.of()
                : club.getMedia().stream()
                .sorted(Comparator
                        .comparing(ClubMedia::getSortOrder)
                        .thenComparing(ClubMedia::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(item -> new MediaDto(
                        item.getId(),
                        item.getUrl(),
                        item.getType().name()
                ))
                .toList();

        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getDescription(),
                club.getScheduleText(),
                club.getRoom(),
                club.getContactEmail(),
                club.getContactPhone(),
                club.getMainImageUrl(),
                club.getIsActive(),
                club.getCreatedBy() != null ? club.getCreatedBy().getId() : null,
                club.getCreatedAt(),
                club.getUpdatedAt(),
                teachers,
                media
        );
    }

    private void attachTeachers(Club club, List<Long> teacherIds) {
        for (Long teacherId : normalizeTeacherIds(teacherIds)) {
            createTeacherRelation(club, getTeacherOrThrow(teacherId));
        }
    }

    private User getTeacherOrThrow(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher with id=" + teacherId + " not found"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id=" + teacherId + " is not a teacher");
        }

        return teacher;
    }

    private void createTeacherRelation(Club club, User teacher) {
        ClubTeacher clubTeacher = new ClubTeacher();
        clubTeacher.setId(new ClubTeacherId(club.getId(), teacher.getId()));
        clubTeacher.setClub(club);
        clubTeacher.setTeacher(teacher);

        clubTeacherRepository.save(clubTeacher);
        club.getTeachers().add(clubTeacher);
    }

    private List<Long> normalizeTeacherIds(List<Long> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(new LinkedHashSet<>(teacherIds));
    }

    private void uploadMainImage(Club club, MultipartFile mainImage, List<String> uploadedUrls) {
        if (!hasFile(mainImage)) {
            return;
        }

        String url = s3StorageService.upload(mainImage, "clubs/" + club.getId() + "/main-image");
        club.setMainImageUrl(url);
        uploadedUrls.add(url);
    }

    private void saveClubMedia(Club club, List<MultipartFile> mediaFiles, List<String> uploadedUrls) {
        List<MultipartFile> files = normalizeFiles(mediaFiles);
        if (files.isEmpty()) {
            return;
        }

        List<ClubMedia> mediaToSave = new ArrayList<>();
        int sortOrder = club.getMedia().size();
        for (MultipartFile file : files) {
            String url = s3StorageService.upload(file, "clubs/" + club.getId() + "/media");
            uploadedUrls.add(url);

            ClubMedia media = new ClubMedia();
            media.setClub(club);
            media.setType(resolveMediaType(file));
            media.setUrl(url);
            media.setSortOrder(sortOrder++);

            mediaToSave.add(media);
            club.getMedia().add(media);
        }

        clubMediaRepository.saveAll(mediaToSave);
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .filter(this::hasFile)
                .toList();
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private MediaType resolveMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("image/")
                ? MediaType.IMAGE
                : MediaType.FILE;
    }

    private void cleanupUploadedFiles(List<String> uploadedUrls) {
        for (String uploadedUrl : uploadedUrls) {
            try {
                s3StorageService.deleteByUrl(uploadedUrl);
            } catch (RuntimeException ignored) {
                // Ignore cleanup failures and preserve the original exception.
            }
        }
    }

    private boolean canViewInactiveClubs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
            return false;
        }

        return hasRole(authentication, UserRole.TEACHER) || hasRole(authentication, UserRole.ADMIN);
    }

    private boolean hasRole(Authentication authentication, UserRole role) {
        String authority = "ROLE_" + role.name();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }
}
