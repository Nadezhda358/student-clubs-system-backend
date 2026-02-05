package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.user.TeacherInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeacherInviteRepository extends JpaRepository<TeacherInvite, Long> {

    Optional<TeacherInvite> findByTokenHash(String tokenHash);

    @Query(
            value = "SELECT * FROM teacher_invites WHERE token_hash = :tokenHash FOR UPDATE",
            nativeQuery = true
    )
    Optional<TeacherInvite> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

}
