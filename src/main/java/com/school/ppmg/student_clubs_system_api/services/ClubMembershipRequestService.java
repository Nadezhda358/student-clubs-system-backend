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
import lombok.RequiredArgsConstructor;
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
                        "Club with id=" + clubId + " not found"
                ));

        if (!Boolean.TRUE.equals(club.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Club is inactive");
        }

        boolean pendingExists = clubMembershipRequestRepository.existsByClub_IdAndStudent_IdAndStatus(
                clubId,
                currentUser.getId(),
                MembershipRequestStatus.PENDING
        );
        if (pendingExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pending membership application already exists for this club"
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
                    "You are already an active member of this club"
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
                    "You are banned from this club"
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
    public List<MembershipApplicationDto> getMyApplications(MembershipRequestStatus status) {
        User currentUser = authService.getCurrentUser();
        requireStudent(currentUser);

        List<ClubMembershipRequest> applications = status == null
                ? clubMembershipRequestRepository.findAllByStudent_IdOrderByCreatedAtDesc(currentUser.getId())
                : clubMembershipRequestRepository.findAllByStudent_IdAndStatusOrderByCreatedAtDesc(
                        currentUser.getId(),
                        status
                );

        return applications.stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MembershipApplicationDto cancelMyApplication(Long id) {
        User currentUser = authService.getCurrentUser();
        requireStudent(currentUser);

        ClubMembershipRequest application = clubMembershipRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membership application with id=" + id + " not found"
                ));

        if (!application.getStudent().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can cancel only your own membership applications");
        }

        if (application.getStatus() != MembershipRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending membership applications can be cancelled");
        }

        application.setStatus(MembershipRequestStatus.CANCELLED);
        return toDto(clubMembershipRequestRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<MembershipApplicationDto> adminGetAll(
            MembershipRequestStatus status,
            Long clubId,
            String q
    ) {
        String normalizedQuery = normalizeQuery(q);

        return clubMembershipRequestRepository.findAllForAdmin(status, clubId, normalizedQuery).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MembershipApplicationDto> teacherGetAll(
            MembershipRequestStatus status,
            Long clubId,
            String q
    ) {
        User teacher = getCurrentTeacher();
        ensureTeacherCanManageClub(teacher, clubId);

        return clubMembershipRequestRepository.findAllForTeacher(
                        teacher.getId(),
                        status,
                        clubId,
                        normalizeQuery(q)
                ).stream()
                .map(this::toDto)
                .toList();
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can manage membership applications");
        }
    }

    private User getCurrentTeacher() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Teacher access required");
        }
        return currentUser;
    }

    private void ensureTeacherCanManageClub(User teacher, Long clubId) {
        if (clubId == null) {
            return;
        }

        if (!clubTeacherRepository.existsByClub_IdAndTeacher_Id(clubId, teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not manage this club");
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
                        "Membership application with id=" + id + " not found"
                ));

        if (application.getStatus() != MembershipRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership application is already decided");
        }

        return application;
    }

    private void validateNewStatus(MembershipRequestStatus newStatus) {
        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        if (newStatus != MembershipRequestStatus.APPROVED && newStatus != MembershipRequestStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be APPROVED or REJECTED");
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
                    "Banned memberships cannot be approved via membership applications"
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
                            "Membership restore failed for club id=" + clubId + " and student id=" + studentId
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
