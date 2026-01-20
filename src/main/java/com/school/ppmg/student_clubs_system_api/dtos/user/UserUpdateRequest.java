package com.school.ppmg.student_clubs_system_api.dtos.user;

import jakarta.validation.constraints.*;

public record UserUpdateRequest(
        @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Min(1) @Max(12) Integer grade,
        @Size(max = 20) String className
) {}