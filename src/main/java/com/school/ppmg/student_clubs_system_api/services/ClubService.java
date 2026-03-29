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
    public ClubDto getById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id=" + id + " not found"));

        if (!Boolean.TRUE.equals(club.getIsActive()) && !canViewInactiveClubs()) {
            throw new ResourceNotFoundException("Club with id=" + id + " not found");
        }

        return toDto(club);
    }

    @Transactional
    public ClubDto create(UpsertClubDto dto) {
        return create(dto, new CreateClubOptions(null, null, null, List.of()));
    }

    @Transactional
    public ClubDto create(UpsertClubDto dto, CreateClubOptions options) {
        if (clubRepository.existsByName(dto.name())) {
            throw new ConflictException("Club name already exists: " + dto.name());
        }

        User currentUser = authService.getCurrentUser();

        Club club = new Club();
        applyUpsert(club, dto);
        club.setCreatedBy(currentUser);
        clubRepository.saveAndFlush(club);

        List<String> uploadedUrls = new ArrayList<>();
        try {
            attachTeacher(club, options.teacherId(), options.teacherIsPrimary());
            uploadMainImage(club, options.mainImage(), uploadedUrls);
            saveClubMedia(club, options.mediaFiles(), uploadedUrls);

            return toDto(clubRepository.save(club));
        } catch (RuntimeException ex) {
            cleanupUploadedFiles(uploadedUrls);
            throw ex;
        }
    }

    @Transactional
    public ClubDto update(Long id, UpsertClubDto dto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id=" + id + " not found"));

        if (!club.getName().equals(dto.name()) && clubRepository.existsByName(dto.name())) {
            throw new ConflictException("Club name already exists: " + dto.name());
        }

        applyUpsert(club, dto);
        return toDto(clubRepository.save(club));
    }

    @Transactional
    public void delete(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new ResourceNotFoundException("Club with id=" + id + " not found");
        }
        clubRepository.deleteById(id);
    }

    @Transactional
    public ClubDto updateMainImage(Long id, MultipartFile file) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id=" + id + " not found"));

        String url = s3StorageService.upload(file, "clubs/" + id + "/main-image");
        club.setMainImageUrl(url);
        return toDto(clubRepository.save(club));
    }

    private void applyUpsert(Club club, UpsertClubDto dto) {
        club.setName(dto.name());
        club.setDescription(dto.description());
        club.setScheduleText(dto.scheduleText());
        club.setRoom(dto.room());
        club.setContactEmail(dto.contactEmail());
        club.setContactPhone(dto.contactPhone());
        club.setIsActive(dto.isActive());
    }

    private ClubListDto toListDto(Club c) {
        return new ClubListDto(c.getId(), c.getName(), c.getRoom(), c.getIsActive(), c.getMainImageUrl());
    }

    private ClubDto toDto(Club c) {
        List<TeacherDto> teachers = c.getTeachers() == null ? java.util.List.of()
                : c.getTeachers().stream()
                .sorted(Comparator
                        .comparing(ClubTeacher::getIsPrimary, Comparator.reverseOrder())
                        .thenComparing(clubTeacher -> clubTeacher.getTeacher().getId()))
                .map(t -> new TeacherDto(
                        t.getTeacher().getId(),
                        t.getTeacher().getFirstName() + " " + t.getTeacher().getLastName(),
                        t.getIsPrimary()
                ))
                .toList();

        List<MediaDto> media = c.getMedia() == null ? java.util.List.of()
                : c.getMedia().stream()
                .sorted(Comparator
                        .comparing(ClubMedia::getSortOrder)
                        .thenComparing(ClubMedia::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(m -> new MediaDto(
                        m.getId(),
                        m.getUrl(),
                        m.getType().name()
                ))
                .toList();

        return new ClubDto(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getScheduleText(),
                c.getRoom(),
                c.getContactEmail(),
                c.getContactPhone(),
                c.getMainImageUrl(),
                c.getIsActive(),
                c.getCreatedBy() != null ? c.getCreatedBy().getId() : null,
                c.getCreatedAt(),
                c.getUpdatedAt(),
                teachers,
                media
        );
    }

    private void attachTeacher(Club club, Long teacherId, Boolean teacherIsPrimary) {
        if (teacherId == null) {
            if (teacherIsPrimary != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "teacherIsPrimary can only be provided when teacherId is set"
                );
            }
            return;
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher with id=" + teacherId + " not found"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with id=" + teacherId + " is not a teacher");
        }

        ClubTeacher clubTeacher = new ClubTeacher();
        clubTeacher.setId(new ClubTeacherId(club.getId(), teacher.getId()));
        clubTeacher.setClub(club);
        clubTeacher.setTeacher(teacher);
        clubTeacher.setIsPrimary(teacherIsPrimary == null ? Boolean.TRUE : teacherIsPrimary);

        clubTeacherRepository.save(clubTeacher);
        club.getTeachers().add(clubTeacher);
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
