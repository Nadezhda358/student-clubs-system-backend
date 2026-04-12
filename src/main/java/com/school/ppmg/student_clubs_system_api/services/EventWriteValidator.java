package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.UpsertEventDto;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

final class EventWriteValidator {

    private EventWriteValidator() {
    }

    static void validateForCreate(UpsertEventDto dto, OffsetDateTime now) {
        validateChronology(dto);
        validateStartAtNotInPast(dto.startAt(), now);
        validateDeadlineNotInPast(dto.registrationDeadline(), now);
    }

    static void validateForUpdate(Event existingEvent, UpsertEventDto dto, OffsetDateTime now) {
        validateChronology(dto);

        if (hasStarted(existingEvent.getStartAt(), now)) {
            if (!sameMoment(existingEvent.getStartAt(), dto.startAt())
                    || !sameMoment(existingEvent.getEndAt(), dto.endAt())
                    || !sameMoment(existingEvent.getRegistrationDeadline(), dto.registrationDeadline())) {
                throw badRequest("Event dates cannot be changed after the event has started");
            }
            return;
        }

        validateStartAtNotInPast(dto.startAt(), now);
        if (!sameMoment(existingEvent.getRegistrationDeadline(), dto.registrationDeadline())) {
            validateDeadlineNotInPast(dto.registrationDeadline(), now);
        }
    }

    static void validateSearchRange(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw badRequest("from must be on/before to");
        }
    }

    private static void validateChronology(UpsertEventDto dto) {
        OffsetDateTime startAt = dto.startAt();
        OffsetDateTime endAt = dto.endAt();
        OffsetDateTime registrationDeadline = dto.registrationDeadline();

        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw badRequest("endAt must be on/after startAt");
        }

        if (startAt != null && registrationDeadline != null && registrationDeadline.isAfter(startAt)) {
            throw badRequest("registrationDeadline must be on/before startAt");
        }
    }

    private static void validateStartAtNotInPast(OffsetDateTime startAt, OffsetDateTime now) {
        if (startAt != null && startAt.isBefore(now)) {
            throw badRequest("startAt must be in the present or future");
        }
    }

    private static void validateDeadlineNotInPast(OffsetDateTime registrationDeadline, OffsetDateTime now) {
        if (registrationDeadline != null && registrationDeadline.isBefore(now)) {
            throw badRequest("registrationDeadline must be in the present or future");
        }
    }

    private static boolean hasStarted(OffsetDateTime startAt, OffsetDateTime now) {
        return startAt != null && !startAt.isAfter(now);
    }

    private static boolean sameMoment(OffsetDateTime left, OffsetDateTime right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.isEqual(right);
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }
}
