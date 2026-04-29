package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMediaRepository extends JpaRepository<ClubMedia, Long> {

    List<ClubMedia> findByClubId(Long clubId);

    Optional<ClubMedia> findByIdAndClub_Id(Long id, Long clubId);
}
