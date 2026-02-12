package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.DashboardDTO;
import ao.gov.sgcd.pm.dto.ProjectProgressDTO;
import ao.gov.sgcd.pm.dto.StakeholderDashboardDTO;
import ao.gov.sgcd.pm.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboards do desenvolvedor e do stakeholder com métricas do projecto")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Obter dashboard do desenvolvedor", description = "Devolve métricas completas do projecto para o painel do desenvolvedor")
    @ApiResponse(responseCode = "200", description = "Dashboard do desenvolvedor devolvido com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping
    public ResponseEntity<DashboardDTO> getDeveloperDashboard() {
        return ResponseEntity.ok(dashboardService.getDeveloperDashboard());
    }

    @Operation(summary = "Obter progresso do projecto", description = "Devolve o estado actual do progresso geral do projecto")
    @ApiResponse(responseCode = "200", description = "Progresso do projecto devolvido com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/progress")
    public ResponseEntity<ProjectProgressDTO> getProjectProgress() {
        return ResponseEntity.ok(dashboardService.getProjectProgress());
    }

    @Operation(summary = "Obter dashboard do stakeholder", description = "Devolve métricas resumidas do projecto para o painel do stakeholder")
    @ApiResponse(responseCode = "200", description = "Dashboard do stakeholder devolvido com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/stakeholder")
    public ResponseEntity<StakeholderDashboardDTO> getStakeholderDashboard() {
        return ResponseEntity.ok(dashboardService.getStakeholderDashboard());
    }
}
