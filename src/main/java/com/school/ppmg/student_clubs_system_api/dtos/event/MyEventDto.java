package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.EventAudience;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import com.school.ppmg.student_clubs_system_api.enums.RegistrationStatus;

import java.time.OffsetDateTime;

public record MyEventDto(
        Long eventId,
        Long clubId,
        String clubName,
        String title,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String location,
        String mainImageUrl,
        EventStatus eventStatus,
        EventAudience audience,
        RegistrationStatus registrationStatus,
        OffsetDateTime registeredAt,
        OffsetDateTime cancelledAt,
        OffsetDateTime registrationDeadline,
        OffsetDateTime effectiveRegistrationDeadline
) {}
