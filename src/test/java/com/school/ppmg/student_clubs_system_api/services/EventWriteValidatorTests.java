package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.UpsertEventDto;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventWriteValidatorTests {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-04-05T10:00:00Z");

    @Test
    void createAllowsFutureMultiDayEvent() {
        UpsertEventDto dto = dto(
                OffsetDateTime.parse("2026-04-05T12:00:00Z"),
                OffsetDateTime.parse("2026-04-06T14:00:00Z"),
                OffsetDateTime.parse("2026-04-05T12:00:00Z")
        );

        assertDoesNotThrow(() -> EventWriteValidator.validateForCreate(dto, NOW));
    }

    @Test
    void createRejectsStartAtInThePast() {
        UpsertEventDto dto = dto(
                OffsetDateTime.parse("2026-04-05T09:59:59Z"),
                OffsetDateTime.parse("2026-04-05T12:00:00Z"),
                OffsetDateTime.parse("2026-04-05T09:00:00Z")
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> EventWriteValidator.validateForCreate(dto, NOW)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("startAt must be in the present or future", ex.getReason());
    }

    @Test
    void createRejectsRegistrationDeadlineInThePast() {
        UpsertEventDto dto = dto(
                OffsetDateTime.parse("2026-04-05T12:00:00Z"),
                OffsetDateTime.parse("2026-04-05T14:00:00Z"),
                OffsetDateTime.parse("2026-04-05T09:59:59Z")
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> EventWriteValidator.validateForCreate(dto, NOW)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("registrationDeadline must be in the present or future", ex.getReason());
    }

    @Test
    void updateAllowsNonDateChangesAfterEventHasStarted() {
        Event existingEvent = event(
                OffsetDateTime.parse("2026-04-04T10:00:00Z"),
                OffsetDateTime.parse("2026-04-06T10:00:00Z"),
                OffsetDateTime.parse("2026-04-04T09:00:00Z")
        );

        UpsertEventDto dto = dto(
                OffsetDateTime.parse("2026-04-04T10:00:00+00:00"),
                OffsetDateTime.parse("2026-04-06T10:00:00+00:00"),
                OffsetDateTime.parse("2026-04-04T09:00:00+00:00")
        );

        assertDoesNotThrow(() -> EventWriteValidator.validateForUpdate(existingEvent, dto, NOW));
    }

    @Test
    void updateRejectsDateChangesAfterEventHasStarted() {
        Event existingEvent = event(
                OffsetDateTime.parse("2026-04-04T10:00:00Z"),
                OffsetDateTime.parse("2026-04-06T10:00:00Z"),
                OffsetDateTime.parse("2026-04-04T09:00:00Z")
        );

        UpsertEventDto dto = dto(
                OffsetDateTime.parse("2026-04-04T10:00:00Z"),
                OffsetDateTime.parse("2026-04-06T11:00:00Z"),
                OffsetDateTime.parse("2026-04-04T09:00:00Z")
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> EventWriteValidator.validateForUpdate(existingEvent, dto, NOW)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Event dates cannot be changed after the event has started", ex.getReason());
    }

    @Test
    void updateBeforeStartAllowsUnchangedPastDeadline() {
        Event existingEvent = event(
                OffsetDateTime.parse("2026-04-06T10:00:00Z"),
                OffsetDateTime.parse("2026-04-06T12:00:00Z"),
                OffsetDateTime.parse("2026-04-04T10:00:00Z")
        );

        UpsertEventDto dto = dto(
                OffsetDateTime.parse("2026-04-06T10:00:00Z"),
                OffsetDateTime.parse("2026-04-06T12:00:00Z"),
                OffsetDateTime.parse("2026-04-04T10:00:00Z")
        );

        assertDoesNotThrow(() -> EventWriteValidator.validateForUpdate(existingEvent, dto, NOW));
    }

    @Test
    void searchRejectsFromAfterTo() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> EventWriteValidator.validateSearchRange(
                        OffsetDateTime.parse("2026-04-06T00:00:00Z"),
                        OffsetDateTime.parse("2026-04-05T00:00:00Z")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("from must be on/before to", ex.getReason());
    }

    private static Event event(OffsetDateTime startAt, OffsetDateTime endAt, OffsetDateTime registrationDeadline) {
        Event event = new Event();
        event.setStartAt(startAt);
        event.setEndAt(endAt);
        event.setRegistrationDeadline(registrationDeadline);
        return event;
    }

    private static UpsertEventDto dto(OffsetDateTime startAt, OffsetDateTime endAt, OffsetDateTime registrationDeadline) {
        return new UpsertEventDto(
                1L,
                "Title",
                "Description",
                startAt,
                endAt,
                "Room 101",
                20,
                registrationDeadline,
                EventStatus.DRAFT,
                EventAudience.ALL_STUDENTS
        );
    }
}
