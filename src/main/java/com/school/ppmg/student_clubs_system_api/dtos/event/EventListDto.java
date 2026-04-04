package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;

import java.time.OffsetDateTime;

public record EventListDto(
        Long id,
        Long clubId,
        String clubName,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String location,
        Integer capacity,
        Long registeredCount,
        Long availableSpots,
        OffsetDateTime registrationDeadline,
        OffsetDateTime effectiveRegistrationDeadline,
        Boolean registrationOpen,
        EventStatus status,
        EventAudience audience
) {}
