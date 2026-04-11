package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubRepository extends JpaRepository<Club, Long> {

    @Query(value = "select count(*) from clubs where deleted_at is null", nativeQuery = true)
    long countUndeleted();

    @Query(value = "select count(*) from clubs where deleted_at is null and is_active = true", nativeQuery = true)
    long countActiveClubs();

    @Query(value = "select count(*) from clubs where deleted_at is null and is_active = false", nativeQuery = true)
    long countInactiveClubs();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<Club> findAllByIsActive(Boolean isActive, Pageable pageable);

    Page<Club> findDistinctByTeachers_Teacher_Id(Long teacherId, Pageable pageable);

    Page<Club> findDistinctByTeachers_Teacher_IdAndIsActive(Long teacherId, Boolean isActive, Pageable pageable);
}
