package com.school.ppmg.student_clubs_system_api.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TeacherInviteRequest(
        @Email @NotBlank String email
) {}
