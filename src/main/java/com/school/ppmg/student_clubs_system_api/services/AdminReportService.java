package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.report.AdminEventsByPeriodDto;
import com.school.ppmg.student_clubs_system_api.dtos.report.AdminEventsByPeriodPointDto;
import com.school.ppmg.student_clubs_system_api.dtos.report.AdminReportsOverviewDto;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.MembershipStatus;
import com.school.ppmg.student_clubs_system_api.enums.ReportPeriod;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRegistrationRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public AdminReportsOverviewDto getOverview(OffsetDateTime from, OffsetDateTime to) {
        requireAdmin();
        EventWriteValidator.validateSearchRange(from, to);

        EventRegistrationRepository.EventParticipationSummaryRow participationSummary =
                eventRegistrationRepository.summarizeParticipationByEventPeriod(from, to);

        return new AdminReportsOverviewDto(
                clubRepository.countUndeleted(),
                clubRepository.countActiveClubs(),
                clubRepository.countInactiveClubs(),
                clubMembershipRepository.countDistinctStudentsByStatus(MembershipStatus.ACTIVE.name()),
                eventRepository.countReportableEventsInPeriod(from, to),
                valueOrZero(participationSummary == null ? null : participationSummary.getRegisteredParticipations()),
                valueOrZero(participationSummary == null ? null : participationSummary.getCancelledParticipations()),
                valueOrZero(participationSummary == null ? null : participationSummary.getUniqueRegisteredParticipants()),
                from,
                to
        );
    }

    @Transactional(readOnly = true)
    public AdminEventsByPeriodDto getEventsByPeriod(
            OffsetDateTime from,
            OffsetDateTime to,
            ReportPeriod period
    ) {
        requireAdmin();
        EventWriteValidator.validateSearchRange(from, to);

        ReportPeriod effectivePeriod = period == null ? ReportPeriod.MONTH : period;
        List<AdminEventsByPeriodPointDto> points = switch (effectivePeriod) {
            case DAY -> eventRepository.countReportableEventsByDay(from, to).stream()
                    .map(this::toPointDto)
                    .toList();
            case WEEK -> eventRepository.countReportableEventsByWeek(from, to).stream()
                    .map(this::toPointDto)
                    .toList();
            case MONTH -> eventRepository.countReportableEventsByMonth(from, to).stream()
                    .map(this::toPointDto)
                    .toList();
        };

        return new AdminEventsByPeriodDto(effectivePeriod, from, to, points);
    }

    private AdminEventsByPeriodPointDto toPointDto(EventRepository.EventCountByPeriodRow row) {
        return new AdminEventsByPeriodPointDto(
                row.getPeriodStart(),
                valueOrZero(row.getEventsCount())
        );
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private User requireAdmin() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return currentUser;
    }
}
