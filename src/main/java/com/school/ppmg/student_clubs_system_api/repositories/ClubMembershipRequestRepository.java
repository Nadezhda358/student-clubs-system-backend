package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipRequest;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ClubMembershipRequestRepository
        extends JpaRepository<ClubMembershipRequest, Long>, JpaSpecificationExecutor<ClubMembershipRequest> {

    @Query(
            value = """
                    select *
                    from club_membership_requests
                    where id = :id
                      and deleted_at is null
                    for update
                    """,
            nativeQuery = true
    )
    Optional<ClubMembershipRequest> findByIdForUpdate(@Param("id") Long id);

    boolean existsByClub_IdAndStudent_IdAndStatus(
            Long clubId,
            Long studentId,
            MembershipRequestStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    update club_membership_requests
                    set deleted_at = :deletedAt
                    where club_id = :clubId
                      and deleted_at is null
                    """,
            nativeQuery = true
    )
    int softDeleteByClubId(@Param("clubId") Long clubId, @Param("deletedAt") OffsetDateTime deletedAt);
}
