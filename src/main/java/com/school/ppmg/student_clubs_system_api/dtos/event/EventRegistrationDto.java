package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;

import java.time.OffsetDateTime;

public record EventRegistrationDto(
        Long eventId,
        Long studentUserId,
        UserSummaryDto student,
        EventStatus status,
        OffsetDateTime registeredAt,
        OffsetDateTime cancelledAt
) {}
