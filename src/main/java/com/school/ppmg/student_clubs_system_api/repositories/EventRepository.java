package com.school.ppmg.student_clubs_system_api.repositories;

import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    interface EventCountByPeriodRow {
        LocalDate getPeriodStart();
        Long getEventsCount();
    }

    @Query(
            value = """
                    select count(*)
                    from events e
                    join clubs c on c.id = e.club_id
                    where e.deleted_at is null
                      and e.status <> 'DRAFT'
                      and (c.deleted_at is null or e.start_at < c.deleted_at)
                      and (:from is null or e.start_at >= :from)
                      and (:to is null or e.start_at <= :to)
                    """,
            nativeQuery = true
    )
    long countReportableEventsInPeriod(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Modifying
    @Query(
            value = """
                    update events
                    set status = 'CANCELLED',
                        updated_at = CURRENT_TIMESTAMP(6)
                    where club_id = :clubId
                      and deleted_at is null
                      and start_at >= :cutoff
                      and status <> 'CANCELLED'
                    """,
            nativeQuery = true
    )
    int cancelFutureEventsForClub(
            @Param("clubId") Long clubId,
            @Param("cutoff") OffsetDateTime cutoff
    );

    @Query(
            value = """
                    select date(e.start_at) as periodStart, count(*) as eventsCount
                    from events e
                    join clubs c on c.id = e.club_id
                    where e.deleted_at is null
                      and e.status <> 'DRAFT'
                      and (c.deleted_at is null or e.start_at < c.deleted_at)
                      and (:from is null or e.start_at >= :from)
                      and (:to is null or e.start_at <= :to)
                    group by date(e.start_at)
                    order by periodStart
                    """,
            nativeQuery = true
    )
    List<EventCountByPeriodRow> countReportableEventsByDay(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query(
            value = """
                    select date_sub(date(e.start_at), interval weekday(e.start_at) day) as periodStart,
                           count(*) as eventsCount
                    from events e
                    join clubs c on c.id = e.club_id
                    where e.deleted_at is null
                      and e.status <> 'DRAFT'
                      and (c.deleted_at is null or e.start_at < c.deleted_at)
                      and (:from is null or e.start_at >= :from)
                      and (:to is null or e.start_at <= :to)
                    group by date_sub(date(e.start_at), interval weekday(e.start_at) day)
                    order by periodStart
                    """,
            nativeQuery = true
    )
    List<EventCountByPeriodRow> countReportableEventsByWeek(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query(
            value = """
                    select cast(date_format(e.start_at, '%Y-%m-01') as date) as periodStart,
                           count(*) as eventsCount
                    from events e
                    join clubs c on c.id = e.club_id
                    where e.deleted_at is null
                      and e.status <> 'DRAFT'
                      and (c.deleted_at is null or e.start_at < c.deleted_at)
                      and (:from is null or e.start_at >= :from)
                      and (:to is null or e.start_at <= :to)
                    group by cast(date_format(e.start_at, '%Y-%m-01') as date)
                    order by periodStart
                    """,
            nativeQuery = true
    )
    List<EventCountByPeriodRow> countReportableEventsByMonth(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
