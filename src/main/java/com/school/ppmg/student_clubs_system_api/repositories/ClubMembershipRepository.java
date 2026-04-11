package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembership;
import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipId;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, ClubMembershipId> {

    List<ClubMembership> findByStudent_Id(Long studentId);

    List<ClubMembership> findByClub_Id(Long clubId);

    Optional<ClubMembership> findByStudent_IdAndClub_Id(Long studentId, Long clubId);

    boolean existsByStudent_IdAndClub_Id(Long studentId, Long clubId);

    boolean existsByClub_IdAndStudent_IdAndStatus(Long clubId, Long studentId, MembershipStatus status);

    @Query(
            value = "select count(*) from club_memberships where club_id = :clubId and student_user_id = :studentId",
            nativeQuery = true
    )
    long countAllByClubIdAndStudentId(@Param("clubId") Long clubId, @Param("studentId") Long studentId);

    @Modifying
    @Query(
            value = """
                    update club_memberships
                    set deleted_at = null,
                        updated_at = CURRENT_TIMESTAMP(6)
                    where club_id = :clubId
                      and student_user_id = :studentId
                    """,
            nativeQuery = true
    )
    int restoreByClubIdAndStudentId(@Param("clubId") Long clubId, @Param("studentId") Long studentId);

    @Modifying
    @Query(
            value = """
                    update club_memberships
                    set status = 'LEFT',
                        left_at = :leftAt,
                        updated_at = CURRENT_TIMESTAMP(6)
                    where club_id = :clubId
                      and deleted_at is null
                      and status = 'ACTIVE'
                    """,
            nativeQuery = true
    )
    int markActiveMembershipsAsLeftForClub(
            @Param("clubId") Long clubId,
            @Param("leftAt") java.time.OffsetDateTime leftAt
    );

    long countByClub_Id(Long clubId);

    @Query(
            value = """
                    select count(distinct student_user_id)
                    from club_memberships cm
                    join clubs c on c.id = cm.club_id
                    where cm.deleted_at is null
                      and c.deleted_at is null
                      and cm.status = :status
                    """,
            nativeQuery = true
    )
    long countDistinctStudentsByStatus(@Param("status") String status);
}
