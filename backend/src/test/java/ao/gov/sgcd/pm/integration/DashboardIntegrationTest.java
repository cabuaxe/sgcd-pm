package ao.gov.sgcd.pm.integration;

import ao.gov.sgcd.pm.dto.DashboardDTO;
import ao.gov.sgcd.pm.dto.ProjectProgressDTO;
import ao.gov.sgcd.pm.dto.StakeholderDashboardDTO;
import ao.gov.sgcd.pm.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DashboardIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Test
    void getDeveloperDashboard_shouldReturnValidData() {
        DashboardDTO dashboard = dashboardService.getDeveloperDashboard();

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalSessions()).isGreaterThan(0);
        assertThat(dashboard.getProjectProgress()).isBetween(0.0, 100.0);
        assertThat(dashboard.getSprintSummaries()).isNotEmpty();
        assertThat(dashboard.getWeekProgress()).isNotNull();
    }

    @Test
    void getProjectProgress_shouldReturnValidData() {
        ProjectProgressDTO progress = dashboardService.getProjectProgress();

        assertThat(progress).isNotNull();
        assertThat(progress.getTotalSessions()).isGreaterThan(0);
        assertThat(progress.getTotalHoursPlanned()).isGreaterThan(0);
        assertThat(progress.getOverallProgress()).isBetween(0.0, 100.0);
        assertThat(progress.getSprints()).isNotEmpty();
    }

    @Test
    void getStakeholderDashboard_shouldReturnValidData() {
        StakeholderDashboardDTO stakeholder = dashboardService.getStakeholderDashboard();

        assertThat(stakeholder).isNotNull();
        assertThat(stakeholder.getProjectName()).isNotBlank();
        assertThat(stakeholder.getClient()).isNotBlank();
        assertThat(stakeholder.getTotalSessions()).isGreaterThan(0);
        assertThat(stakeholder.getSprints()).isNotEmpty();
        assertThat(stakeholder.getMilestones()).isNotEmpty();
    }
}
