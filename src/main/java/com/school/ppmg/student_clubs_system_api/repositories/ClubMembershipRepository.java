package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembership;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipId;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, ClubMembershipId> {

    List<ClubMembership> findByStudent_Id(Long studentId);

    List<ClubMembership> findByClub_Id(Long clubId);

    Optional<ClubMembership> findByStudent_IdAndClub_Id(Long studentId, Long clubId);

    boolean existsByStudent_IdAndClub_Id(Long studentId, Long clubId);

    boolean existsByClub_IdAndStudent_IdAndStatus(Long clubId, Long studentId, MembershipStatus status);

    long countByClub_Id(Long clubId);
}
