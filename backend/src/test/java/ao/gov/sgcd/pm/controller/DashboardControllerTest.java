package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.DashboardDTO;
import ao.gov.sgcd.pm.dto.StakeholderDashboardDTO;
import ao.gov.sgcd.pm.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void getDeveloperDashboard_shouldReturn200WithDashboard() throws Exception {
        DashboardDTO dashboard = DashboardDTO.builder()
                .projectProgress(25.5)
                .totalSessions(204)
                .completedSessions(52)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.valueOf(175.5))
                .recentTasks(List.of())
                .sprintSummaries(List.of(
                        DashboardDTO.SprintSummaryDTO.builder()
                                .sprintNumber(1)
                                .name("Sprint 1 - Fundacao")
                                .progress(100.0)
                                .status("COMPLETED")
                                .color("#CC092F")
                                .build()
                ))
                .upcomingBlockedDays(List.of())
                .weekProgress(DashboardDTO.WeekProgressDTO.builder()
                        .weekTasks(5)
                        .weekCompleted(3)
                        .weekHoursPlanned(BigDecimal.valueOf(17.5))
                        .weekHoursSpent(BigDecimal.valueOf(10.5))
                        .build())
                .build();

        when(dashboardService.getDeveloperDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectProgress", is(25.5)))
                .andExpect(jsonPath("$.totalSessions", is(204)))
                .andExpect(jsonPath("$.completedSessions", is(52)))
                .andExpect(jsonPath("$.totalHoursPlanned", is(680)))
                .andExpect(jsonPath("$.sprintSummaries", hasSize(1)))
                .andExpect(jsonPath("$.sprintSummaries[0].sprintNumber", is(1)))
                .andExpect(jsonPath("$.sprintSummaries[0].name", is("Sprint 1 - Fundacao")))
                .andExpect(jsonPath("$.sprintSummaries[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$.weekProgress.weekTasks", is(5)))
                .andExpect(jsonPath("$.weekProgress.weekCompleted", is(3)));
    }

    @Test
    void getDeveloperDashboard_withNoData_shouldReturn200() throws Exception {
        DashboardDTO emptyDashboard = DashboardDTO.builder()
                .projectProgress(0.0)
                .totalSessions(204)
                .completedSessions(0)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.ZERO)
                .recentTasks(List.of())
                .sprintSummaries(List.of())
                .upcomingBlockedDays(List.of())
                .weekProgress(DashboardDTO.WeekProgressDTO.builder()
                        .weekTasks(0)
                        .weekCompleted(0)
                        .weekHoursPlanned(BigDecimal.ZERO)
                        .weekHoursSpent(BigDecimal.ZERO)
                        .build())
                .build();

        when(dashboardService.getDeveloperDashboard()).thenReturn(emptyDashboard);

        mockMvc.perform(get("/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectProgress", is(0.0)))
                .andExpect(jsonPath("$.completedSessions", is(0)));
    }

    @Test
    void getStakeholderDashboard_shouldReturn200WithDashboard() throws Exception {
        StakeholderDashboardDTO stakeholderDashboard = StakeholderDashboardDTO.builder()
                .projectName("SGCD - Sistema de Gestao Consular Digital")
                .client("Embaixada da Republica de Angola")
                .overallProgress(25.5)
                .totalSessions(204)
                .completedSessions(52)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.valueOf(175.5))
                .startDate(LocalDate.of(2026, 3, 2))
                .targetDate(LocalDate.of(2026, 12, 20))
                .daysRemaining(200L)
                .sprints(List.of(
                        StakeholderDashboardDTO.StakeholderSprintDTO.builder()
                                .number(1)
                                .name("Sprint 1 - Fundacao")
                                .nameEn("Sprint 1 - Foundation")
                                .progress(100.0)
                                .status("COMPLETED")
                                .startDate(LocalDate.of(2026, 3, 2))
                                .endDate(LocalDate.of(2026, 4, 13))
                                .sessions(36)
                                .completedSessions(36)
                                .hours(120)
                                .hoursSpent(BigDecimal.valueOf(120))
                                .color("#CC092F")
                                .focus("Backend foundation")
                                .build()
                ))
                .milestones(List.of(
                        StakeholderDashboardDTO.MilestoneDTO.builder()
                                .name("Sprint 1 Complete")
                                .targetDate(LocalDate.of(2026, 4, 13))
                                .status("COMPLETED")
                                .build()
                ))
                .weeklyActivity(StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                        .sessionsThisWeek(5)
                        .hoursThisWeek(BigDecimal.valueOf(17.5))
                        .tasksCompletedThisWeek(3)
                        .build())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(dashboardService.getStakeholderDashboard()).thenReturn(stakeholderDashboard);

        mockMvc.perform(get("/v1/dashboard/stakeholder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName", is("SGCD - Sistema de Gestao Consular Digital")))
                .andExpect(jsonPath("$.client", is("Embaixada da Republica de Angola")))
                .andExpect(jsonPath("$.overallProgress", is(25.5)))
                .andExpect(jsonPath("$.totalSessions", is(204)))
                .andExpect(jsonPath("$.completedSessions", is(52)))
                .andExpect(jsonPath("$.totalHoursPlanned", is(680)))
                .andExpect(jsonPath("$.daysRemaining", is(200)))
                .andExpect(jsonPath("$.sprints", hasSize(1)))
                .andExpect(jsonPath("$.sprints[0].number", is(1)))
                .andExpect(jsonPath("$.sprints[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$.milestones", hasSize(1)))
                .andExpect(jsonPath("$.milestones[0].name", is("Sprint 1 Complete")))
                .andExpect(jsonPath("$.weeklyActivity.sessionsThisWeek", is(5)))
                .andExpect(jsonPath("$.lastUpdated", notNullValue()));
    }

    @Test
    void getStakeholderDashboard_shouldIncludeStartAndTargetDates() throws Exception {
        StakeholderDashboardDTO dashboard = StakeholderDashboardDTO.builder()
                .projectName("SGCD")
                .client("Embaixada")
                .overallProgress(0.0)
                .totalSessions(204)
                .completedSessions(0)
                .totalHoursPlanned(680)
                .totalHoursSpent(BigDecimal.ZERO)
                .startDate(LocalDate.of(2026, 3, 2))
                .targetDate(LocalDate.of(2026, 12, 20))
                .daysRemaining(300L)
                .sprints(List.of())
                .milestones(List.of())
                .weeklyActivity(StakeholderDashboardDTO.WeeklyActivityDTO.builder()
                        .sessionsThisWeek(0)
                        .hoursThisWeek(BigDecimal.ZERO)
                        .tasksCompletedThisWeek(0)
                        .build())
                .lastUpdated(LocalDateTime.now())
                .build();

        when(dashboardService.getStakeholderDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/v1/dashboard/stakeholder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate", is("2026-03-02")))
                .andExpect(jsonPath("$.targetDate", is("2026-12-20")));
    }
}
