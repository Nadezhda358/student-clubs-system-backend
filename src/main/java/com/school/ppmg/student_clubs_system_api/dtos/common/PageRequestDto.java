package com.school.ppmg.student_clubs_system_api.dtos.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequestDto(
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}