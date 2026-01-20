package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;
import com.school.ppmg.student_clubs_system_api.enums.EventStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record EventDto(
        Long id,
        Long clubId,
        String title,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String location,
        Integer capacity,
        OffsetDateTime registrationDeadline,
        EventStatus status,

        UserSummaryDto createdBy,
        List<EventMediaDto> media
) {}
