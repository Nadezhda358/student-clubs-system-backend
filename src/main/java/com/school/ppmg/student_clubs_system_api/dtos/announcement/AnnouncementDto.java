package com.school.ppmg.student_clubs_system_api.dtos.announcement;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;

import java.time.OffsetDateTime;

public record AnnouncementDto(
        Long id,
        Long clubId,
        String title,
        String body,
        boolean isPublished,
        OffsetDateTime publishedAt,
        UserSummaryDto author
) {}
