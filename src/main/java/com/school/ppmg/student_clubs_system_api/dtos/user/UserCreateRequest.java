package com.school.ppmg.student_clubs_system_api.dtos.user;

import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotNull UserRole role,

        // student-only fields (you can validate conditionally in service)
        @Min(1) @Max(12) Integer grade,
        @Size(max = 20) String className
) {}