package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.UpsertEventDto;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventWriteValidatorTest {

    @Test
    void validateForCreateRejectsPublishedEventsStartingInThePast() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-29T10:00:00+03:00");

        assertThatThrownBy(() -> EventWriteValidator.validateForCreate(
                eventDto(now.minusHours(1), now.plusHours(1), now.minusMinutes(30), EventStatus.PUBLISHED),
                now
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void validateForUpdateRejectsEditingPublishedEventsAfterTheyStart() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-29T10:00:00+03:00");
        Event existing = new Event();
        existing.setStatus(EventStatus.PUBLISHED);
        existing.setStartAt(now.minusMinutes(5));

        assertThatThrownBy(() -> EventWriteValidator.validateForUpdate(
                existing,
                eventDto(now.plusDays(1), now.plusDays(1).plusHours(2), now.plusHours(10), EventStatus.PUBLISHED),
                now
        ))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void validateSearchRangeRejectsFromAfterTo() {
        OffsetDateTime from = OffsetDateTime.parse("2026-05-01T12:00:00+03:00");
        OffsetDateTime to = OffsetDateTime.parse("2026-05-01T10:00:00+03:00");

        assertThatThrownBy(() -> EventWriteValidator.validateSearchRange(from, to))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
    }

    private UpsertEventDto eventDto(
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            OffsetDateTime registrationDeadline,
            EventStatus status
    ) {
        return new UpsertEventDto(
                1L,
                "Работилница",
                "Описание",
                startAt,
                endAt,
                "Кабинет 101",
                20,
                registrationDeadline,
                status,
                EventAudience.ALL_STUDENTS
        );
    }
}
