package com.school.ppmg.student_clubs_system_api.dtos.common;
import java.util.List;

public record PageResponse<T>(
        List<T> items,
        long total,
        int page,
        int size
) {}