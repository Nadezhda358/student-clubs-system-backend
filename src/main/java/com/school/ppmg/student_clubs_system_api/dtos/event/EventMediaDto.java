package com.school.ppmg.student_clubs_system_api.dtos.event;

import com.school.ppmg.student_clubs_system_api.enums.MediaType;

public record EventMediaDto(
        Long id,
        MediaType type,
        String url,
        String caption,
        Integer sortOrder
) {}