package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.CreateMembershipApplicationRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.MembershipApplicationDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembership;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipId;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipRequest;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRequestRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMembershipRequestService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final ClubMembershipRequestRepository clubMembershipRequestRepository;
    private final ClubTeacherRepository clubTeacherRepository;
    private final AuthService authService;

    @Transactional
    public MembershipApplicationDto apply(Long clubId, CreateMembershipApplicationRequest request) {
        User currentUser = authService.getCurrentUser();
        requireStudent(currentUser);

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Клуб с id=" + clubId + " не е намерен"
                ));

        if (!Boolean.TRUE.equals(club.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Клубът е неактивен");
        }

        boolean pendingExists = clubMembershipRequestRepository.existsByClub_IdAndStudent_IdAndStatus(
                clubId,
                currentUser.getId(),
                MembershipRequestStatus.PENDING
        );
        if (pendingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Вече има чакаща кандидатура за членство в този клуб"
            );
        }

        boolean activeMembershipExists = clubMembershipRepository.existsByClub_IdAndStudent_IdAndStatus(
                clubId,
                currentUser.getId(),
                MembershipStatus.ACTIVE
        );
        if (activeMembershipExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Вече сте активен член на този клуб"
            );
        }

        boolean bannedMembershipExists = clubMembershipRepository.existsByClub_IdAndStudent_IdAndStatus(
                clubId,
                currentUser.getId(),
                MembershipStatus.BANNED
        );
        if (bannedMembershipExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Достъпът ви до този клуб е забранен"
            );
        }

        ClubMembershipRequest application = new ClubMembershipRequest();
        application.setClub(club);
        application.setStudent(currentUser);
        application.setStatus(MembershipRequestStatus.PENDING);
        application.setMessage(request.motivationText());

        return toDto(clubMembershipRequestRepository.save(application));
    }

    @Transactional(readOnly = true)
    public Page<MembershipApplicationDto> getMyApplications(
            MembershipRequestStatus status,
            Long clubId,
            String q,
            Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser();
        requireStudent(currentUser);

        Page<ClubMembershipRequest> page = clubMembershipRequestRepository.findAll(
                myApplicationsSpecification(currentUser.getId(), status, clubId, normalizeQuery(q)),
                withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return page.map(this::toDto);
    }

    @Transactional
    public MembershipApplicationDto cancelMyApplication(Long id) {
        User currentUser = authService.getCurrentUser();
        requireStudent(currentUser);

        ClubMembershipRequest application = clubMembershipRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Кандидатура за членство с id=" + id + " не е намерена"
                ));

        if (!application.getStudent().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Можете да отменяте само собствените си кандидатури за членство");
        }

        if (application.getStatus() != MembershipRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Само чакащи кандидатури за членство могат да бъдат отменяни");
        }

        application.setStatus(MembershipRequestStatus.CANCELLED);
        return toDto(clubMembershipRequestRepository.save(application));
    }

    @Transactional(readOnly = true)
    public Page<MembershipApplicationDto> adminGetAll(
            MembershipRequestStatus status,
            Long clubId,
            String q,
            Pageable pageable
    ) {
        Page<ClubMembershipRequest> page = clubMembershipRequestRepository.findAll(
                managementApplicationsSpecification(null, status, clubId, normalizeQuery(q)),
                withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<MembershipApplicationDto> teacherGetAll(
            MembershipRequestStatus status,
            Long clubId,
            String q,
            Pageable pageable
    ) {
        User teacher = getCurrentTeacher();
        ensureTeacherCanManageClub(teacher, clubId);

        Page<ClubMembershipRequest> page = clubMembershipRequestRepository.findAll(
                managementApplicationsSpecification(teacher.getId(), status, clubId, normalizeQuery(q)),
                withDefaultSort(pageable, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return page.map(this::toDto);
    }

    @Transactional
    public MembershipApplicationDto adminUpdateStatus(Long id, MembershipRequestStatus newStatus) {
        return updateStatus(id, newStatus, authService.getCurrentUser());
    }

    @Transactional
    public MembershipApplicationDto teacherUpdateStatus(Long id, MembershipRequestStatus newStatus) {
        User teacher = getCurrentTeacher();
        ClubMembershipRequest application = getPendingApplicationOrThrow(id, newStatus);
        ensureTeacherCanManageClub(teacher, application.getClub().getId());
        return finalizeStatusUpdate(application, newStatus, teacher);
    }

    private void requireStudent(User user) {
        if (user.getRole() != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Само ученици могат да управляват кандидатури за членство");
        }
    }

    private User getCurrentTeacher() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Необходим е учителски достъп");
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

    private MembershipApplicationDto updateStatus(Long id, MembershipRequestStatus newStatus, User decidedBy) {
        ClubMembershipRequest application = getPendingApplicationOrThrow(id, newStatus);
        return finalizeStatusUpdate(application, newStatus, decidedBy);
    }

    private ClubMembershipRequest getPendingApplicationOrThrow(Long id, MembershipRequestStatus newStatus) {
        validateNewStatus(newStatus);

        ClubMembershipRequest application = clubMembershipRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Кандидатура за членство с id=" + id + " не е намерена"
                ));

        if (application.getStatus() != MembershipRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "По тази кандидатура за членство вече има решение");
        }

        return application;
    }

    private void validateNewStatus(MembershipRequestStatus newStatus) {
        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статусът е задължителен");
        }

        if (newStatus != MembershipRequestStatus.APPROVED && newStatus != MembershipRequestStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статусът трябва да означава одобрение или отхвърляне");
        }
    }

    private MembershipApplicationDto finalizeStatusUpdate(
            ClubMembershipRequest application,
            MembershipRequestStatus newStatus,
            User decidedBy
    ) {
        OffsetDateTime decidedAt = OffsetDateTime.now();
        application.setStatus(newStatus);
        application.setDecidedBy(decidedBy);
        application.setDecidedAt(decidedAt);

        if (newStatus == MembershipRequestStatus.APPROVED) {
            activateClubMembership(application, decidedAt);
        }

        return toDto(clubMembershipRequestRepository.save(application));
    }

    private void activateClubMembership(ClubMembershipRequest application, OffsetDateTime approvedAt) {
        Long clubId = application.getClub().getId();
        Long studentId = application.getStudent().getId();

        MembershipActivation activation = resolveMembershipForActivation(application, clubId, studentId);
        ClubMembership membership = activation.membership();

        if (membership.getStatus() == MembershipStatus.BANNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Забранени членства не могат да бъдат одобрявани чрез кандидатури за членство"
            );
        }

        boolean shouldRefreshJoinedAt = activation.restored()
                || membership.getStatus() != MembershipStatus.ACTIVE
                || membership.getLeftAt() != null
                || membership.getJoinedAt() == null;

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setLeftAt(null);
        if (shouldRefreshJoinedAt) {
            membership.setJoinedAt(approvedAt);
        }

        clubMembershipRepository.save(membership);
    }

    private MembershipActivation resolveMembershipForActivation(
            ClubMembershipRequest application,
            Long clubId,
            Long studentId
    ) {
        ClubMembership existingMembership = clubMembershipRepository.findByStudent_IdAndClub_Id(studentId, clubId)
                .orElse(null);
        if (existingMembership != null) {
            return new MembershipActivation(existingMembership, false);
        }

        if (clubMembershipRepository.countAllByClubIdAndStudentId(clubId, studentId) > 0) {
            clubMembershipRepository.restoreByClubIdAndStudentId(clubId, studentId);

            ClubMembership restoredMembership = clubMembershipRepository.findByStudent_IdAndClub_Id(studentId, clubId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Възстановяването на членство не успя за клуб с id=" + clubId + " и ученик с id=" + studentId
                    ));

            return new MembershipActivation(restoredMembership, true);
        }

        ClubMembership membership = new ClubMembership();
        membership.setId(new ClubMembershipId(clubId, studentId));
        membership.setClub(application.getClub());
        membership.setStudent(application.getStudent());
        return new MembershipActivation(membership, false);
    }

    private String normalizeQuery(String q) {
        return (q == null || q.isBlank()) ? null : q.trim();
    }

    private Specification<ClubMembershipRequest> myApplicationsSpecification(
            Long studentId,
            MembershipRequestStatus status,
            Long clubId,
            String q
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("student").get("id"), studentId));
            predicates.add(cb.isNull(club.get("deletedAt")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (clubId != null) {
                predicates.add(cb.equal(club.get("id"), clubId));
            }

            if (q != null) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(club.get("name")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("message"), "")), like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<ClubMembershipRequest> managementApplicationsSpecification(
            Long teacherId,
            MembershipRequestStatus status,
            Long clubId,
            String q
    ) {
        return (root, query, cb) -> {
            Join<Object, Object> club = root.join("club");
            Join<Object, Object> student = root.join("student");
            List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isNull(club.get("deletedAt")));
            predicates.add(cb.isNull(student.get("deletedAt")));

            if (teacherId != null) {
                query.distinct(true);
                predicates.add(cb.equal(club.join("teachers").get("teacher").get("id"), teacherId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (clubId != null) {
                predicates.add(cb.equal(club.get("id"), clubId));
            }

            if (q != null) {
                String like = "%" + q.toLowerCase() + "%";
                Expression<String> fullName = cb.lower(
                        cb.concat(cb.concat(cb.coalesce(student.get("firstName"), ""), " "), cb.coalesce(student.get("lastName"), ""))
                );
                predicates.add(cb.or(
                        cb.like(cb.lower(club.get("name")), like),
                        cb.like(cb.lower(student.get("email")), like),
                        cb.like(fullName, like),
                        cb.like(cb.lower(cb.coalesce(root.get("message"), "")), like)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable == null || pageable.isUnpaged() || pageable.getSort().isSorted()) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }

    private MembershipApplicationDto toDto(ClubMembershipRequest application) {
        return new MembershipApplicationDto(
                application.getId(),
                application.getClub().getId(),
                application.getClub().getName(),
                application.getStudent().getId(),
                application.getStudent().getFirstName() + " " + application.getStudent().getLastName(),
                application.getStudent().getEmail(),
                application.getStatus(),
                application.getMessage(),
                application.getCreatedAt()
        );
    }

    private record MembershipActivation(ClubMembership membership, boolean restored) {
    }
}
