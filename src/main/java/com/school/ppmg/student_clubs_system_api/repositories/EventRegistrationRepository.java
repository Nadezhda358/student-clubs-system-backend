package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistration;
import com.school.ppmg.student_clubs_system_api.entities.event.EventRegistrationId;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface EventRegistrationRepository
        extends JpaRepository<EventRegistration, EventRegistrationId>, JpaSpecificationExecutor<EventRegistration> {

    interface EventParticipationSummaryRow {
        Long getRegisteredParticipations();
        Long getCancelledParticipations();
        Long getUniqueRegisteredParticipants();
    }

    long countByEvent_IdAndStatus(Long eventId, RegistrationStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = """
                    update event_registrations
                    set deleted_at = :deletedAt,
                        updated_at = CURRENT_TIMESTAMP(6)
                    where event_id = :eventId
                      and deleted_at is null
                    """,
            nativeQuery = true
    )
    int softDeleteByEventId(
            @Param("eventId") Long eventId,
            @Param("deletedAt") OffsetDateTime deletedAt
    );

    @Query(
            value = """
                    select coalesce(sum(case when er.status = 'REGISTERED' then 1 else 0 end), 0) as registeredParticipations,
                           coalesce(sum(case when er.status = 'CANCELLED' then 1 else 0 end), 0) as cancelledParticipations,
                           coalesce(count(distinct case when er.status = 'REGISTERED' then er.student_user_id end), 0)
                               as uniqueRegisteredParticipants
                    from event_registrations er
                    join events e on e.id = er.event_id
                    join clubs c on c.id = e.club_id
                    where er.deleted_at is null
                      and e.deleted_at is null
                      and e.status <> 'DRAFT'
                      and (c.deleted_at is null or e.start_at < c.deleted_at)
                      and (:from is null or e.start_at >= :from)
                      and (:to is null or e.start_at <= :to)
                    """,
            nativeQuery = true
    )
    EventParticipationSummaryRow summarizeParticipationByEventPeriod(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
