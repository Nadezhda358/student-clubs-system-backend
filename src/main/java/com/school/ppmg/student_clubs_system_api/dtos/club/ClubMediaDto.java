package com.school.ppmg.student_clubs_system_api.dtos.club;


import com.school.ppmg.student_clubs_system_api.enums.MediaType;

public record ClubMediaDto(
        Long id,
        MediaType type,
        String url,
        String caption,
        Integer sortOrder
) {}
