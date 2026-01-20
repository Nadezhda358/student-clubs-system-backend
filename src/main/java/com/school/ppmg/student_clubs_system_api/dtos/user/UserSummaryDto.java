package com.school.ppmg.student_clubs_system_api.dtos.user;

import com.school.ppmg.student_clubs_system_api.enums.UserRole;

public record UserSummaryDto(
        Long id,
        String firstName,
        String lastName,
        UserRole role,
        Integer grade,
        String className
) {}