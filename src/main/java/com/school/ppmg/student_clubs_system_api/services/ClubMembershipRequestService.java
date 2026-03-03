package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.club.CreateMembershipApplicationRequest;
import com.school.ppmg.student_clubs_system_api.dtos.club.MembershipApplicationDto;
import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipRequest;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRequestRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
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
    private final ClubMembershipRequestRepository clubMembershipRequestRepository;
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

        boolean approvedExists = clubMembershipRequestRepository.existsByClub_IdAndStudent_IdAndStatus(
                clubId,
                currentUser.getId(),
                MembershipRequestStatus.APPROVED
        );
        if (approvedExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You are already an approved member of this club"
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

    @Transactional(readOnly = true)
    public List<MembershipApplicationDto> adminGetAll(
            MembershipRequestStatus status,
            Long clubId,
            String q
    ) {
        String normalizedQuery = (q == null || q.isBlank()) ? null : q.trim();

        return clubMembershipRequestRepository.findAllForAdmin(status, clubId, normalizedQuery).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MembershipApplicationDto adminUpdateStatus(Long id, MembershipRequestStatus newStatus) {
        if (newStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        if (newStatus != MembershipRequestStatus.APPROVED && newStatus != MembershipRequestStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status must be APPROVED or REJECTED");
        }

        ClubMembershipRequest application = clubMembershipRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membership application with id=" + id + " not found"
                ));

        if (application.getStatus() != MembershipRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Membership application is already decided");
        }

        application.setStatus(newStatus);
        application.setDecidedBy(authService.getCurrentUser());
        application.setDecidedAt(OffsetDateTime.now());

        return toDto(clubMembershipRequestRepository.save(application));
    }

    private void requireStudent(User user) {
        if (user.getRole() != UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can manage membership applications");
        }
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
}
