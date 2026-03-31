package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<Club> findAllByIsActive(Boolean isActive, Pageable pageable);

    Page<Club> findDistinctByTeachers_Teacher_Id(Long teacherId, Pageable pageable);

    Page<Club> findDistinctByTeachers_Teacher_IdAndIsActive(Long teacherId, Boolean isActive, Pageable pageable);
}
