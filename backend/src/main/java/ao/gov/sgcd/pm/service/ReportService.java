package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.ReportDTO;
import ao.gov.sgcd.pm.entity.*;
import ao.gov.sgcd.pm.mapper.ReportMapper;
import ao.gov.sgcd.pm.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SprintReportRepository reportRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;
    private final PdfExportService pdfExportService;

    public List<ReportDTO> findAll() {
        return reportMapper.toDtoList(reportRepository.findAllByOrderByGeneratedAtDesc());
    }

    public ReportDTO findBySprintId(Long sprintId) {
        List<SprintReport> reports = reportRepository.findBySprintIdOrderByGeneratedAtDesc(sprintId);
        if (reports.isEmpty()) {
            throw new RuntimeException("Relatório não encontrado para sprint: " + sprintId);
        }
        return reportMapper.toDto(reports.get(0));
    }

    @Transactional
    public ReportDTO generateReport(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrado: " + sprintId));

        int completed = taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.COMPLETED);
        int blocked = taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.BLOCKED);
        int skipped = taskRepository.countBySprintIdAndStatus(sprintId, TaskStatus.SKIPPED);

        double progress = sprint.getTotalSessions() > 0
                ? (completed * 100.0) / sprint.getTotalSessions() : 0;

        // Build metrics JSON
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSessions", sprint.getTotalSessions());
        metrics.put("completedSessions", completed);
        metrics.put("blockedTasks", blocked);
        metrics.put("skippedTasks", skipped);
        metrics.put("totalHours", sprint.getTotalHours());
        metrics.put("actualHours", sprint.getActualHours());
        metrics.put("progressPercent", progress);

        String metricsJson;
        try {
            metricsJson = objectMapper.writeValueAsString(metrics);
        } catch (Exception e) {
            metricsJson = "{}";
        }

        String summaryPt = String.format(
                "Sprint %d (%s): %d/%d sessões concluídas (%.1f%%). Horas: %s/%d. %d tarefas bloqueadas, %d ignoradas.",
                sprint.getSprintNumber(), sprint.getName(),
                completed, sprint.getTotalSessions(), progress,
                sprint.getActualHours(), sprint.getTotalHours(),
                blocked, skipped);

        String summaryEn = String.format(
                "Sprint %d (%s): %d/%d sessions completed (%.1f%%). Hours: %s/%d. %d blocked, %d skipped.",
                sprint.getSprintNumber(), sprint.getNameEn(),
                completed, sprint.getTotalSessions(), progress,
                sprint.getActualHours(), sprint.getTotalHours(),
                blocked, skipped);

        SprintReport report = SprintReport.builder()
                .sprint(sprint)
                .reportType(ReportType.SPRINT_END)
                .generatedAt(LocalDateTime.now())
                .summaryPt(summaryPt)
                .summaryEn(summaryEn)
                .metricsJson(metricsJson)
                .build();

        return reportMapper.toDto(reportRepository.save(report));
    }

    public ReportDTO findById(Long id) {
        SprintReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado: " + id));
        return reportMapper.toDto(report);
    }

    public byte[] generatePdf(Long reportId) {
        SprintReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado: " + reportId));
        return pdfExportService.generatePdf(report);
    }
}
