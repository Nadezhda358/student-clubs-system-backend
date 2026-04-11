package com.school.ppmg.student_clubs_system_api.dtos.report;

import java.time.LocalDate;

public record AdminEventsByPeriodPointDto(
        LocalDate periodStart,
        Long eventsCount
) {}
