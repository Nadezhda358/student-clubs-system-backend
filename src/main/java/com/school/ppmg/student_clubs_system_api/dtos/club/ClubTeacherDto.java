package com.school.ppmg.student_clubs_system_api.dtos.club;

import com.school.ppmg.student_clubs_system_api.dtos.user.UserSummaryDto;

public record ClubTeacherDto(
        Long teacherUserId,
        UserSummaryDto teacher,
        boolean isPrimary
) {}
