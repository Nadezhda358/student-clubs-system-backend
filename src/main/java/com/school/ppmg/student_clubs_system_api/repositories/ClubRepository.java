package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByName(String name);

    boolean existsByName(String name);
}

