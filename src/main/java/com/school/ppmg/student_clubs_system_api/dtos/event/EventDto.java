package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;

import java.time.OffsetDateTime;

public record EventDto(
        Long id,
        Long clubId,
        String clubName,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String location,
        String mainImageUrl,
        Integer capacity,
        Long registeredCount,
        Long availableSpots,
        OffsetDateTime registrationDeadline,
        OffsetDateTime effectiveRegistrationDeadline,
        Boolean registrationOpen,
        EventStatus status,
        EventAudience audience,
        Long createdById,
        String createdByName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
