package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.event.UpsertEventDto;
import com.school.ppmg.student_clubs_system_api.entities.event.Event;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

final class EventWriteValidator {

    private EventWriteValidator() {
    }

    static void validateForCreate(UpsertEventDto dto, OffsetDateTime now) {
        validateChronology(dto);
        if (dto.status() == EventStatus.PUBLISHED) {
            validatePublishableDates(dto, now);
        }
    }

    static void validateForUpdate(Event existingEvent, UpsertEventDto dto, OffsetDateTime now) {
        validateChronology(dto);

        boolean existingDatePassed = hasStarted(existingEvent.getStartAt(), now);

        if (existingEvent.getStatus() == EventStatus.PUBLISHED && existingDatePassed) {
            throw badRequest("Published events cannot be edited after they have started. Create a new event if this schedule needs to change.");
        }

        if (existingEvent.getStatus() == EventStatus.CANCELLED && existingDatePassed) {
            validatePastCancelledUpdate(existingEvent, dto);
            return;
        }

        if (dto.status() == EventStatus.PUBLISHED) {
            validatePublishableDates(dto, now);
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

    private static void validatePublishableDates(UpsertEventDto dto, OffsetDateTime now) {
        validateStartAtNotInPast(dto.startAt(), now);
        validateDeadlineNotInPast(dto.registrationDeadline(), now);
    }

    private static void validatePastCancelledUpdate(Event existingEvent, UpsertEventDto dto) {
        if (dto.status() != EventStatus.CANCELLED) {
            throw badRequest("Cancelled events whose original start date has passed must stay CANCELLED. Create a new event if you want to schedule it again.");
        }

        if (!sameMoment(existingEvent.getStartAt(), dto.startAt())
                || !sameMoment(existingEvent.getEndAt(), dto.endAt())
                || !sameMoment(existingEvent.getRegistrationDeadline(), dto.registrationDeadline())) {
            throw badRequest("Cancelled events whose original start date has passed cannot be rescheduled. Create a new event with the new dates instead.");
        }
    }

    private static void validateStartAtNotInPast(OffsetDateTime startAt, OffsetDateTime now) {
        if (startAt != null && startAt.isBefore(now)) {
            throw badRequest("Published events must have startAt in the present or future. Save it as DRAFT if the date is still being planned.");
        }
    }

    private static void validateDeadlineNotInPast(OffsetDateTime registrationDeadline, OffsetDateTime now) {
        if (registrationDeadline != null && registrationDeadline.isBefore(now)) {
            throw badRequest("Published events must have registrationDeadline in the present or future. Move the deadline forward before publishing.");
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
