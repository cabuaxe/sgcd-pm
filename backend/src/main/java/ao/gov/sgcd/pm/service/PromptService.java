package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.PromptDTO;
import ao.gov.sgcd.pm.entity.Sprint;
import ao.gov.sgcd.pm.entity.Task;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.exception.ResourceNotFoundException;
import ao.gov.sgcd.pm.repository.TaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    private static final String PROJECT_CONTEXT = """
            SGCD — Sistema de Gestão Consular Digital
            Embaixada da República de Angola — Alemanha & Rep. Checa

            STACK: Spring Boot 3.x (Java 21), Angular 17+, MySQL 8.0
                   Kafka, Redis, MinIO, Keycloak, Docker Compose
                   Hetzner Cloud (Alemanha, RGPD)

            REPOS: sgcd-backend (Maven multi-module), sgcd-frontend-backoffice,
                   sgcd-frontend-portal, sgcd-infra, sgcd-docs

            MODULES: commons, gateway, registration-svc, scheduling-svc,
                     passport-svc, civil-registry-svc, financial-svc, workflow-svc

            CONVENTIONS:
            - Google Java Style (Checkstyle)
            - Conventional Commits (commitlint + Husky)
            - Coverage: minimum 80%
            - MapStruct for DTO mapping
            - Flyway for DB migrations
            - OpenAPI/Swagger for API docs
            - PT for comments, EN for technical names
            - Testcontainers for integration tests""";

    public PromptDTO getPromptForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + taskId));
        return buildPrompt(task);
    }

    public PromptDTO getTodayPrompt() {
        LocalDate today = LocalDate.now();
        Task task = taskRepository.findBySessionDate(today)
                .orElseGet(() -> {
                    List<Task> upcoming = taskRepository.findUpcomingPlanned(today);
                    return upcoming.isEmpty() ? null : upcoming.get(0);
                });

        if (task == null) {
            return PromptDTO.builder()
                    .prompt("Nenhuma tarefa encontrada para hoje.")
                    .build();
        }

        return buildPrompt(task);
    }

    public String getProjectContext() {
        return PROJECT_CONTEXT;
    }

    private PromptDTO buildPrompt(Task task) {
        Sprint sprint = task.getSprint();
        int totalCompleted = taskRepository.countByStatus(TaskStatus.COMPLETED);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Recent completed
        List<Task> recentCompleted = taskRepository.findRecentCompleted(PageRequest.of(0, 3));
        StringBuilder recentStr = new StringBuilder();
        for (Task t : recentCompleted) {
            recentStr.append("• ").append(t.getTaskCode()).append(": ").append(t.getTitle()).append("\n");
        }

        // Deliverables
        List<String> deliverables = parseJson(task.getDeliverables());
        StringBuilder delStr = new StringBuilder();
        for (String d : deliverables) {
            delStr.append("■ ").append(d).append("\n");
        }

        // Validation
        List<String> validation = parseJson(task.getValidationCriteria());
        StringBuilder valStr = new StringBuilder();
        for (String v : validation) {
            valStr.append("✓ ").append(v).append("\n");
        }

        String prompt = String.format("""
                ═══════════════════════════════════════════
                SGCD Development Session
                Sprint %d: %s · %s · %s
                Session %d of 204 · %sh (%s)
                ═══════════════════════════════════════════

                PROJECT CONTEXT:
                %s

                SPRINT %d STATUS:
                • Focus: %s
                • Progress: %d/%d sessions (%d%%)
                • Recently completed:
                %s
                TODAY'S TASK: %s
                %s

                DELIVERABLES:
                %s
                VALIDATION:
                %s
                Coverage target: %s

                DELIVERY RULES:
                1. Production-quality code only
                2. Unit tests for all new code
                3. Integration tests where applicable
                4. Portuguese for comments, English for technical names
                5. Follow existing patterns and conventions
                """,
                sprint.getSprintNumber(), sprint.getName(), task.getTaskCode(),
                task.getSessionDate().format(df),
                totalCompleted + 1, task.getPlannedHours(), task.getDayOfWeek(),
                PROJECT_CONTEXT,
                sprint.getSprintNumber(), sprint.getFocus(),
                sprint.getCompletedSessions(), sprint.getTotalSessions(),
                sprint.getTotalSessions() > 0 ? (sprint.getCompletedSessions() * 100) / sprint.getTotalSessions() : 0,
                recentStr,
                task.getTitle(),
                task.getDescription() != null ? task.getDescription() : "",
                delStr,
                valStr,
                task.getCoverageTarget()
        );

        return PromptDTO.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .title(task.getTitle())
                .prompt(prompt)
                .build();
    }

    private List<String> parseJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
