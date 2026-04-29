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
            throw badRequest("Публикувани събития не могат да се редактират след началото им. Създайте ново събитие, ако графикът трябва да се промени.");
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
            throw badRequest("Началната дата трябва да е на или преди крайната дата");
        }
    }

    private static void validateChronology(UpsertEventDto dto) {
        OffsetDateTime startAt = dto.startAt();
        OffsetDateTime endAt = dto.endAt();
        OffsetDateTime registrationDeadline = dto.registrationDeadline();

        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw badRequest("Крайният час трябва да е на или след началния");
        }

        if (startAt != null && registrationDeadline != null && registrationDeadline.isAfter(startAt)) {
            throw badRequest("Крайният срок за записване трябва да е на или преди началото");
        }
    }

    private static void validatePublishableDates(UpsertEventDto dto, OffsetDateTime now) {
        validateStartAtNotInPast(dto.startAt(), now);
        validateDeadlineNotInPast(dto.registrationDeadline(), now);
    }

    private static void validatePastCancelledUpdate(Event existingEvent, UpsertEventDto dto) {
        if (dto.status() != EventStatus.CANCELLED) {
            throw badRequest("Отменени събития, чиято първоначална начална дата е минала, трябва да останат отменени. Създайте ново събитие, ако искате да го планирате отново.");
        }

        if (!sameMoment(existingEvent.getStartAt(), dto.startAt())
                || !sameMoment(existingEvent.getEndAt(), dto.endAt())
                || !sameMoment(existingEvent.getRegistrationDeadline(), dto.registrationDeadline())) {
            throw badRequest("Отменени събития, чиято първоначална начална дата е минала, не могат да бъдат пренасрочвани. Вместо това създайте ново събитие с новите дати.");
        }
    }

    private static void validateStartAtNotInPast(OffsetDateTime startAt, OffsetDateTime now) {
        if (startAt != null && startAt.isBefore(now)) {
            throw badRequest("Публикуваните събития трябва да имат начало сега или в бъдеще. Запазете събитието като чернова, ако датата още се планира.");
        }
    }

    private static void validateDeadlineNotInPast(OffsetDateTime registrationDeadline, OffsetDateTime now) {
        if (registrationDeadline != null && registrationDeadline.isBefore(now)) {
            throw badRequest("Публикуваните събития трябва да имат краен срок за записване сега или в бъдеще. Преместете срока напред преди публикуване.");
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
