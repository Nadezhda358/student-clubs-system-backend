package com.school.ppmg.student_clubs_system_api.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TeacherInviteRequest(
        @NotEmpty
        List<@Email String> emails
) {}
