package com.school.ppmg.student_clubs_system_api.dtos.club;

import jakarta.validation.constraints.*;

public record ClubUpdateRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 5000) String description,
        @Size(max = 2000) String scheduleText,
        @Size(max = 80) String room,
        @Email @Size(max = 255) String contactEmail,
        @Pattern(regexp = "^[0-9+\\-()\\s]{6,40}$", message = "Invalid phone format")
        @Size(max = 40) String contactPhone,
        @NotNull Boolean isActive
) {}