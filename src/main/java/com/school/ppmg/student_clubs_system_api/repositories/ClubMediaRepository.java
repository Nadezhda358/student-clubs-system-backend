package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMediaRepository extends JpaRepository<ClubMedia, Long> {

    List<ClubMedia> findByClubId(Long clubId);
}
