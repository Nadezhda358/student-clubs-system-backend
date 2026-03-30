package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacher;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubTeacherRepository extends JpaRepository<ClubTeacher, ClubTeacherId> {

    List<ClubTeacher> findByClubId(Long clubId);

    List<ClubTeacher> findByTeacherId(Long teacherId);

    Optional<ClubTeacher> findByClubIdAndTeacherId(Long clubId, Long teacherId);

    boolean existsByClubIdAndTeacherId(Long clubId, Long teacherId);
}
