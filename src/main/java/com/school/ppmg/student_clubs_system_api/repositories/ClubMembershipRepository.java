package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    List<ClubMembership> findByStudentId(Long studentId);

    List<ClubMembership> findByClubId(Long clubId);

    Optional<ClubMembership> findByStudentIdAndClubId(Long studentId, Long clubId);

    boolean existsByStudentIdAndClubId(Long studentId, Long clubId);

    long countByClubId(Long clubId);
}
