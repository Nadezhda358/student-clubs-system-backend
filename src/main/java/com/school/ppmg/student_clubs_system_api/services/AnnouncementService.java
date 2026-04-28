package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.announcement.AnnouncementDto;
import com.school.ppmg.student_clubs_system_api.dtos.announcement.UpsertAnnouncementDto;
import com.school.ppmg.student_clubs_system_api.entities.announcement.Announcement;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.exceptions.ResourceNotFoundException;
import com.school.ppmg.student_clubs_system_api.repositories.AnnouncementRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubTeacherRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClubRepository clubRepository;
    private final ClubTeacherRepository clubTeacherRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public Page<AnnouncementDto> getPublicAnnouncements(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        EventWriteValidator.validateSearchRange(from, to);

        Page<Announcement> page = announcementRepository.findAll(
                publicAnnouncementsSpecification(clubId, normalizeQuery(q), from, to),
                withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"))
        );

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public AnnouncementDto getPublicAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findOne(publicAnnouncementByIdSpecification(id))
                .orElseThrow(() -> new ResourceNotFoundException("Съобщение с id=" + id + " не е намерено"));

        return toDto(announcement);
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementDto> getTeacherAnnouncements(
            Long clubId,
            Boolean published,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        User teacher = getCurrentTeacher();
        ensureTeacherCanManageClub(teacher, clubId);
        EventWriteValidator.validateSearchRange(from, to);

        Page<Announcement> page = announcementRepository.findAll(
                managementAnnouncementsSpecification(teacher.getId(), clubId, published, normalizeQuery(q), from, to),
                withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"))
        );

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public AnnouncementDto getTeacherAnnouncementById(Long id) {
        return toDto(getTeacherManagedAnnouncementOrThrow(id));
    }

    @Transactional
    public AnnouncementDto createTeacherAnnouncement(UpsertAnnouncementDto dto) {
        User teacher = getCurrentTeacher();
        Club club = getClubOrThrow(dto.clubId());
        ensureTeacherCanManageClub(teacher, club.getId());

        Announcement announcement = new Announcement();
        announcement.setAuthor(teacher);
        applyUpsert(announcement, dto);

        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementDto updateTeacherAnnouncement(Long id, UpsertAnnouncementDto dto) {
        User teacher = getCurrentTeacher();
        Announcement announcement = getTeacherManagedAnnouncementOrThrow(id);
        Club club = getClubOrThrow(dto.clubId());
        ensureTeacherCanManageClub(teacher, club.getId());

        applyUpsert(announcement, dto);
        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteTeacherAnnouncement(Long id) {
        announcementRepository.delete(getTeacherManagedAnnouncementOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementDto> getAdminAnnouncements(
            Long clubId,
            Boolean published,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        requireAdmin();
        EventWriteValidator.validateSearchRange(from, to);

        Page<Announcement> page = announcementRepository.findAll(
                managementAnnouncementsSpecification(null, clubId, published, normalizeQuery(q), from, to),
                withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"))
        );

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public AnnouncementDto getAdminAnnouncementById(Long id) {
        requireAdmin();
        return toDto(getAnnouncementWithAvailableClubOrThrow(id));
    }

    @Transactional
    public AnnouncementDto createAdminAnnouncement(UpsertAnnouncementDto dto) {
        User admin = requireAdmin();
        Club club = getClubOrThrow(dto.clubId());

        Announcement announcement = new Announcement();
        announcement.setAuthor(admin);
        applyUpsert(announcement, dto);

        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementDto updateAdminAnnouncement(Long id, UpsertAnnouncementDto dto) {
        requireAdmin();
        Announcement announcement = getAnnouncementWithAvailableClubOrThrow(id);
        applyUpsert(announcement, dto);
        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteAdminAnnouncement(Long id) {
        requireAdmin();
        announcementRepository.delete(getAnnouncementWithAvailableClubOrThrow(id));
    }

    private Announcement getAnnouncementWithAvailableClubOrThrow(Long id) {
        return announcementRepository.findOne(managementAnnouncementByIdSpecification(id))
                .orElseThrow(() -> new ResourceNotFoundException("Съобщение с id=" + id + " не е намерено"));
    }

    private Announcement getTeacherManagedAnnouncementOrThrow(Long id) {
        Announcement announcement = getAnnouncementWithAvailableClubOrThrow(id);
        ensureTeacherCanManageClub(getCurrentTeacher(), announcement.getClub().getId());
        return announcement;
    }

    private Club getClubOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Клуб с id=" + id + " не е намерен"));
    }

    private void applyUpsert(Announcement announcement, UpsertAnnouncementDto dto) {
        Club club = getClubOrThrow(dto.clubId());
        announcement.setClub(club);
        announcement.setTitle(dto.title());
        announcement.setBody(dto.body());
        applyPublishedState(announcement, dto.isPublished());
    }

    private void applyPublishedState(Announcement announcement, Boolean isPublished) {
        if (Boolean.TRUE.equals(isPublished)) {
            announcement.setIsPublished(true);
            if (announcement.getPublishedAt() == null) {
                announcement.setPublishedAt(OffsetDateTime.now());
            }
            return;
        }

        announcement.setIsPublished(false);
        announcement.setPublishedAt(null);
    }

    private Specification<Announcement> publicAnnouncementsSpecification(
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            List<Predicate> predicates = new ArrayList<>();
            requireAvailableClub(predicates, cb, club);
            predicates.add(cb.isTrue(root.get("isPublished")));
            predicates.add(cb.isTrue(club.get("isActive")));

            applyCommonFilters(predicates, cb, root, club, clubId, q, from, to, true);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Announcement> publicAnnouncementByIdSpecification(Long id) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            return cb.and(
                    cb.equal(root.get("id"), id),
                    cb.isNotNull(club.get("id")),
                    cb.isNull(club.get("deletedAt")),
                    cb.isTrue(root.get("isPublished")),
                    cb.isTrue(club.get("isActive"))
            );
        };
    }

    private Specification<Announcement> managementAnnouncementByIdSpecification(Long id) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id"), id));
            requireAvailableClub(predicates, cb, club);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Announcement> managementAnnouncementsSpecification(
            Long teacherId,
            Long clubId,
            Boolean published,
            String q,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            List<Predicate> predicates = new ArrayList<>();
            requireAvailableClub(predicates, cb, club);

            if (teacherId != null) {
                query.distinct(true);
                predicates.add(cb.equal(club.join("teachers").get("teacher").get("id"), teacherId));
            }

            if (published != null) {
                predicates.add(cb.equal(root.get("isPublished"), published));
            }

            applyCommonFilters(predicates, cb, root, club, clubId, q, from, to, false);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void requireAvailableClub(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            Join<Object, Object> club
    ) {
        predicates.add(cb.isNotNull(club.get("id")));
        predicates.add(cb.isNull(club.get("deletedAt")));
    }

    private void applyCommonFilters(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<Announcement> root,
            Join<Object, Object> club,
            Long clubId,
            String q,
            OffsetDateTime from,
            OffsetDateTime to,
            boolean usePublishedAt
    ) {
        if (clubId != null) {
            predicates.add(cb.equal(club.get("id"), clubId));
        }

        Expression<OffsetDateTime> effectiveTimestamp = usePublishedAt
                ? root.get("publishedAt")
                : cb.coalesce(root.get("publishedAt"), root.get("createdAt"));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(effectiveTimestamp, from));
        }

        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(effectiveTimestamp, to));
        }

        if (q != null) {
            String like = "%" + q.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("body")), like),
                    cb.like(cb.lower(club.get("name")), like)
            ));
        }
    }

    private AnnouncementDto toDto(Announcement announcement) {
        User author = announcement.getAuthor();
        return new AnnouncementDto(
                announcement.getId(),
                announcement.getClub().getId(),
                announcement.getClub().getName(),
                announcement.getTitle(),
                announcement.getBody(),
                announcement.getIsPublished(),
                announcement.getPublishedAt(),
                author != null ? author.getId() : null,
                author != null ? author.getFirstName() + " " + author.getLastName() : null,
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }

    private User getCurrentTeacher() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Необходим е учителски достъп");
        }
        return currentUser;
    }

    private User requireAdmin() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Необходим е администраторски достъп");
        }
        return currentUser;
    }

    private void ensureTeacherCanManageClub(User teacher, Long clubId) {
        if (clubId == null) {
            return;
        }

        if (!clubTeacherRepository.existsByClub_IdAndTeacher_Id(clubId, teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Не управлявате този клуб");
        }
    }

    private Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable == null || pageable.isUnpaged() || pageable.getSort().isSorted()) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }

    private String normalizeQuery(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }
}
