package com.school.ppmg.student_clubs_system_api.controllers;

import com.school.ppmg.student_clubs_system_api.dtos.report.AdminEventsByPeriodDto;
import com.school.ppmg.student_clubs_system_api.dtos.report.AdminClubParticipantsByClubDto;
import com.school.ppmg.student_clubs_system_api.dtos.report.AdminReportsOverviewDto;
import com.school.ppmg.student_clubs_system_api.enums.ReportPeriod;
import com.school.ppmg.student_clubs_system_api.services.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/overview")
    public AdminReportsOverviewDto getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return adminReportService.getOverview(from, to);
    }

    @GetMapping("/events-by-period")
    public AdminEventsByPeriodDto getEventsByPeriod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false) ReportPeriod period
    ) {
        return adminReportService.getEventsByPeriod(from, to, period);
    }

    @GetMapping("/participants-by-club")
    public List<AdminClubParticipantsByClubDto> getParticipantsByClub() {
        return adminReportService.getParticipantsByClub();
    }
}
