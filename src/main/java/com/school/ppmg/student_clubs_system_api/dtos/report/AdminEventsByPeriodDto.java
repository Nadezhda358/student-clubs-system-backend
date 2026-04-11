package com.school.ppmg.student_clubs_system_api.dtos.report;

import com.school.ppmg.student_clubs_system_api.enums.ReportPeriod;

import java.time.OffsetDateTime;
import java.util.List;

public record AdminEventsByPeriodDto(
        ReportPeriod period,
        OffsetDateTime from,
        OffsetDateTime to,
        List<AdminEventsByPeriodPointDto> points
) {}
