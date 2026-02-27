package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipRequest;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRequestRepository extends JpaRepository<ClubMembershipRequest, Long> {

    Optional<ClubMembershipRequest> findByStudentIdAndClubId(Long studentId, Long clubId);

    boolean existsByStudentIdAndClubIdAndStatus(
            Long studentId,
            Long clubId,
            MembershipRequestStatus status
    );

    boolean existsByClub_IdAndStudent_IdAndStatus(
            Long clubId,
            Long studentId,
            MembershipRequestStatus status
    );

    List<ClubMembershipRequest> findByClubIdAndStatus(
            Long clubId,
            MembershipRequestStatus status
    );

    List<ClubMembershipRequest> findByStudentId(Long studentId);

    List<ClubMembershipRequest> findAllByStudent_IdOrderByCreatedAtDesc(Long studentId);

    List<ClubMembershipRequest> findAllByStudent_IdAndStatusOrderByCreatedAtDesc(
            Long studentId,
            MembershipRequestStatus status
    );
}
