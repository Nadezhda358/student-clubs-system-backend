package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.EventStatus;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record EventUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,

        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,

        @Size(max = 200) String location,
        @Min(1) @Max(100000) Integer capacity,

        OffsetDateTime registrationDeadline,
        @NotNull EventStatus status
) {}