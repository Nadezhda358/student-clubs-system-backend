package com.school.ppmg.student_clubs_system_api.services;

import com.school.ppmg.student_clubs_system_api.dtos.report.AdminEventsByPeriodDto;
import com.school.ppmg.student_clubs_system_api.dtos.report.AdminReportsOverviewDto;
import com.school.ppmg.student_clubs_system_api.entities.user.User;
import com.school.ppmg.student_clubs_system_api.enums.ReportPeriod;
import com.school.ppmg.student_clubs_system_api.enums.UserRole;
import com.school.ppmg.student_clubs_system_api.repositories.ClubMembershipRepository;
import com.school.ppmg.student_clubs_system_api.repositories.ClubRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRegistrationRepository;
import com.school.ppmg.student_clubs_system_api.repositories.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubMembershipRepository clubMembershipRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventRegistrationRepository eventRegistrationRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminReportService adminReportService;

    @Test
    void getOverviewAggregatesRepositoryValues() {
        OffsetDateTime from = OffsetDateTime.parse("2026-04-01T00:00:00+03:00");
        OffsetDateTime to = OffsetDateTime.parse("2026-04-30T23:59:59+03:00");

        when(authService.getCurrentUser()).thenReturn(createUser(UserRole.ADMIN));
        when(clubRepository.countUndeleted()).thenReturn(12L);
        when(clubRepository.countActiveClubs()).thenReturn(9L);
        when(clubRepository.countInactiveClubs()).thenReturn(3L);
        when(clubMembershipRepository.countDistinctStudentsByStatus("ACTIVE")).thenReturn(47L);
        when(eventRepository.countReportableEventsInPeriod(from, to)).thenReturn(15L);
        when(eventRegistrationRepository.summarizeParticipationByEventPeriod(from, to))
                .thenReturn(summaryRow(29L, 4L, 24L));

        AdminReportsOverviewDto overview = adminReportService.getOverview(from, to);

        assertThat(overview.totalClubs()).isEqualTo(12L);
        assertThat(overview.activeClubs()).isEqualTo(9L);
        assertThat(overview.inactiveClubs()).isEqualTo(3L);
        assertThat(overview.activeMembers()).isEqualTo(47L);
        assertThat(overview.totalEvents()).isEqualTo(15L);
        assertThat(overview.registeredParticipations()).isEqualTo(29L);
        assertThat(overview.cancelledParticipations()).isEqualTo(4L);
        assertThat(overview.uniqueRegisteredParticipants()).isEqualTo(24L);
        assertThat(overview.from()).isEqualTo(from);
        assertThat(overview.to()).isEqualTo(to);
    }

    @Test
    void getEventsByPeriodDefaultsToMonthWhenNoPeriodIsProvided() {
        OffsetDateTime from = OffsetDateTime.parse("2026-01-01T00:00:00+02:00");
        OffsetDateTime to = OffsetDateTime.parse("2026-06-30T23:59:59+03:00");

        when(authService.getCurrentUser()).thenReturn(createUser(UserRole.ADMIN));
        when(eventRepository.countReportableEventsByMonth(from, to))
                .thenReturn(List.of(periodRow(LocalDate.parse("2026-05-01"), 7L)));

        AdminEventsByPeriodDto report = adminReportService.getEventsByPeriod(from, to, null);

        assertThat(report.period()).isEqualTo(ReportPeriod.MONTH);
        assertThat(report.points()).singleElement().satisfies(point -> {
            assertThat(point.periodStart()).isEqualTo(LocalDate.parse("2026-05-01"));
            assertThat(point.eventsCount()).isEqualTo(7L);
        });
    }

    @Test
    void getOverviewRejectsNonAdminUsers() {
        when(authService.getCurrentUser()).thenReturn(createUser(UserRole.TEACHER));

        assertThatThrownBy(() -> adminReportService.getOverview(null, null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(403));
    }

    private User createUser(UserRole role) {
        User user = new User();
        user.setId(1L);
        user.setEmail(role.name().toLowerCase() + "@example.com");
        user.setFirstName("Тест");
        user.setLastName("Потребител");
        user.setPasswordHash("hash");
        user.setRole(role);
        return user;
    }

    private EventRegistrationRepository.EventParticipationSummaryRow summaryRow(
            Long registered,
            Long cancelled,
            Long uniqueParticipants
    ) {
        return new EventRegistrationRepository.EventParticipationSummaryRow() {
            @Override
            public Long getRegisteredParticipations() {
                return registered;
            }

            @Override
            public Long getCancelledParticipations() {
                return cancelled;
            }

            @Override
            public Long getUniqueRegisteredParticipants() {
                return uniqueParticipants;
            }
        };
    }

    private EventRepository.EventCountByPeriodRow periodRow(LocalDate date, Long count) {
        return new EventRepository.EventCountByPeriodRow() {
            @Override
            public LocalDate getPeriodStart() {
                return date;
            }

            @Override
            public Long getEventsCount() {
                return count;
            }
        };
    }
}
