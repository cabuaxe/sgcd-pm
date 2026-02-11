package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.DashboardDTO;
import ao.gov.sgcd.pm.dto.ProjectProgressDTO;
import ao.gov.sgcd.pm.dto.StakeholderDashboardDTO;
import ao.gov.sgcd.pm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDeveloperDashboard() {
        return ResponseEntity.ok(dashboardService.getDeveloperDashboard());
    }

    @GetMapping("/progress")
    public ResponseEntity<ProjectProgressDTO> getProjectProgress() {
        return ResponseEntity.ok(dashboardService.getProjectProgress());
    }

    @GetMapping("/stakeholder")
    public ResponseEntity<StakeholderDashboardDTO> getStakeholderDashboard() {
        return ResponseEntity.ok(dashboardService.getStakeholderDashboard());
    }
}
