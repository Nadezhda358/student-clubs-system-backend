package com.school.ppmg.student_clubs_system_api.dtos.club;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;

import java.util.List;

public record ClubDto(
        Long id,
        String name,
        String description,
        String scheduleText,
        String room,
        String contactEmail,
        String contactPhone,
        boolean isActive,

        UserSummaryDto createdBy,
        List<ClubTeacherDto> teachers,
        List<ClubMediaDto> media
) {}