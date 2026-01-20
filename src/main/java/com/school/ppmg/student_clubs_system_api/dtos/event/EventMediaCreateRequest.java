package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.MediaType;
import jakarta.validation.constraints.*;

public record EventMediaCreateRequest(
        @NotNull MediaType type,
        @NotBlank @Size(max = 2048) String url,
        @Size(max = 255) String caption,
        @Min(0) @Max(1000) Integer sortOrder
) {}
