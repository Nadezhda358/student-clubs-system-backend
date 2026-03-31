package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.*;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.exceptions.ConflictException;
import com.school.ppmg.student_clubs_system_api.exceptions.ResourceNotFoundException;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubTeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubTeacherRepository clubTeacherRepository;
    private final AuthService authService;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public Page<ClubListDto> getAll(Boolean active, Pageable pageable) {
        Page<Club> page = active == null
                ? clubRepository.findAll(pageable)
                : clubRepository.findAllByIsActive(active, pageable);

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
        return toDto(getClubOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ClubDto getManagedById(Long id) {
        return toDto(getManagedClubOrThrow(id));
    }

    @Transactional
    public ClubDto create(UpsertClubDto dto) {
        if (clubRepository.existsByName(dto.name())) {
            throw new ConflictException("Club name already exists: " + dto.name());
        }

        Club club = new Club();
        applyUpsert(club, dto);
        club.setCreatedBy(authService.getCurrentUser());
        return toDto(clubRepository.save(club));
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

    private ClubListDto toListDto(Club c) {
        return new ClubListDto(c.getId(), c.getName(), c.getRoom(), c.getIsActive(), c.getMainImageUrl());
    }

    private ClubDto toDto(Club c) {
        List<TeacherDto> teachers = c.getTeachers() == null ? java.util.List.of()
                : c.getTeachers().stream()
                .map(t -> new TeacherDto(
                        t.getTeacher().getId(),
                        t.getTeacher().getFirstName() + " " + t.getTeacher().getLastName()
                ))
                .toList();

        List<MediaDto> media = c.getMedia() == null ? java.util.List.of()
                : c.getMedia().stream()
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
}
