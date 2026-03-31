package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacher;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubTeacherRepository extends JpaRepository<ClubTeacher, ClubTeacherId> {

    List<ClubTeacher> findByClub_Id(Long clubId);

    List<ClubTeacher> findByTeacher_Id(Long teacherId);

    Optional<ClubTeacher> findByClub_IdAndTeacher_Id(Long clubId, Long teacherId);

    boolean existsByClub_IdAndTeacher_Id(Long clubId, Long teacherId);
}
