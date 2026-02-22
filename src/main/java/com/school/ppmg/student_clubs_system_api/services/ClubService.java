package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.*;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.exceptions.ResourceNotFoundException;
import com.school.ppmg.student_clubs_system_api.exceptions.ConflictException;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;

    @Transactional(readOnly = true)
    public Page<ClubListDto> getAll(Boolean active, Pageable pageable) {
        Page<Club> page = active == null
                ? clubRepository.findAll(pageable)
                : clubRepository.findAllByIsActive(active, pageable);

        return page.map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public ClubDto getById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club with id=" + id + " not found"));
        return toDto(club);
    }

    @Transactional
    public ClubDto create(UpsertClubDto dto) {
        if (clubRepository.existsByName(dto.name())) {
            throw new ConflictException("Club name already exists: " + dto.name());
        }

        if (dto.createdById() == null) {
            throw new ConflictException("createdById is required");
        }

        User createdBy = userRepository.findById(dto.createdById())
                .orElseThrow(() -> new ResourceNotFoundException("User with id=" + dto.createdById() + " not found"));

        Club club = new Club();
        applyUpsert(club, dto);
        club.setCreatedBy(createdBy);
        return toDto(clubRepository.save(club));
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
