package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.announcement.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, JpaSpecificationExecutor<Announcement> {

    @Modifying
    @Query(
            value = """
                    update announcements
                    set deleted_at = :deletedAt
                    where club_id = :clubId
                      and deleted_at is null
                    """,
            nativeQuery = true
    )
    int softDeleteByClubId(
            @Param("clubId") Long clubId,
            @Param("deletedAt") OffsetDateTime deletedAt
    );
}
