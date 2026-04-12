package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacher;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubTeacherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubTeacherRepository extends JpaRepository<ClubTeacher, ClubTeacherId> {

    List<ClubTeacher> findByClub_Id(Long clubId);

    List<ClubTeacher> findByTeacher_Id(Long teacherId);

    Optional<ClubTeacher> findByClub_IdAndTeacher_Id(Long clubId, Long teacherId);

    boolean existsByClub_IdAndTeacher_Id(Long clubId, Long teacherId);

    @Query(
            value = "select count(*) from club_teachers where club_id = :clubId and teacher_user_id = :teacherId",
            nativeQuery = true
    )
    long countAllByClubIdAndTeacherId(@Param("clubId") Long clubId, @Param("teacherId") Long teacherId);

    @Modifying
    @Query(
            value = """
                    update club_teachers
                    set deleted_at = null,
                        updated_at = CURRENT_TIMESTAMP(6)
                    where club_id = :clubId
                      and teacher_user_id = :teacherId
                    """,
            nativeQuery = true
    )
    int restoreByClubIdAndTeacherId(@Param("clubId") Long clubId, @Param("teacherId") Long teacherId);
}
