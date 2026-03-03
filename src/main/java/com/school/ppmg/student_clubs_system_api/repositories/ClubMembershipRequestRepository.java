package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.club.ClubMembershipRequest;
import com.school.ppmg.student_clubs_system_api.enums.MembershipRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRequestRepository extends JpaRepository<ClubMembershipRequest, Long> {

    Optional<ClubMembershipRequest> findByStudentIdAndClubId(Long studentId, Long clubId);

    boolean existsByStudentIdAndClubIdAndStatus(
            Long studentId,
            Long clubId,
            MembershipRequestStatus status
    );

    boolean existsByClub_IdAndStudent_IdAndStatus(
            Long clubId,
            Long studentId,
            MembershipRequestStatus status
    );

    List<ClubMembershipRequest> findByClubIdAndStatus(
            Long clubId,
            MembershipRequestStatus status
    );

    List<ClubMembershipRequest> findByStudentId(Long studentId);

    List<ClubMembershipRequest> findAllByStudent_IdOrderByCreatedAtDesc(Long studentId);

    List<ClubMembershipRequest> findAllByStudent_IdAndStatusOrderByCreatedAtDesc(
            Long studentId,
            MembershipRequestStatus status
    );

    @Query("""
            select r
            from ClubMembershipRequest r
            join r.club c
            join r.student s
            where (:status is null or r.status = :status)
              and (:clubId is null or c.id = :clubId)
              and (
                    :q is null
                    or lower(c.name) like lower(concat('%', :q, '%'))
                    or lower(s.email) like lower(concat('%', :q, '%'))
                    or lower(concat(concat(coalesce(s.firstName, ''), ' '), coalesce(s.lastName, '')))
                        like lower(concat('%', :q, '%'))
                  )
            order by r.createdAt desc
            """)
    List<ClubMembershipRequest> findAllForAdmin(
            @Param("status") MembershipRequestStatus status,
            @Param("clubId") Long clubId,
            @Param("q") String q
    );
}
