package com.school.ppmg.student_clubs_system_api.dtos.auth;

import java.time.OffsetDateTime;

public record TeacherInviteResponse(
        Long id,
        String email,
        OffsetDateTime expiresAt
) {}
