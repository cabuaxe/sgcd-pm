package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.WebhookConfig;
import ao.gov.sgcd.pm.entity.Task;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.repository.TaskRepository;
import ao.gov.sgcd.pm.service.TaskService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints para integração com GitHub via webhooks")
public class WebhookController {

    private final WebhookConfig webhookConfig;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    private static final Pattern TASK_CODE_PATTERN = Pattern.compile("S(\\d+)-(\\d+)");
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Operation(summary = "GitHub Webhook", description = "Recebe eventos do GitHub (push, pull_request) e actualiza automaticamente o estado das tarefas correspondentes")
    @PostMapping("/github")
    public ResponseEntity<Map<String, Object>> handleGitHubWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Event", required = false) String event,
            @RequestBody String payload) {

        // 1. Validate HMAC signature
        if (!verifySignature(payload, signature)) {
            log.warn("Webhook recebido com assinatura inválida");
            return ResponseEntity.status(401).body(Map.of("error", "Assinatura inválida"));
        }

        // 2. Parse and route by event type
        try {
            JsonNode root = objectMapper.readTree(payload);
            List<String> updated = new ArrayList<>();

            if ("push".equals(event)) {
                updated = handlePushEvent(root);
            } else if ("pull_request".equals(event)) {
                updated = handlePullRequestEvent(root);
            } else {
                log.info("Evento GitHub ignorado: {}", event);
                return ResponseEntity.ok(Map.of("status", "ignored", "event", String.valueOf(event)));
            }

            log.info("Webhook processado: evento={}, tarefas_actualizadas={}", event, updated);
            return ResponseEntity.ok(Map.of(
                    "status", "processed",
                    "event", event,
                    "tasks_updated", updated
            ));

        } catch (Exception e) {
            log.error("Erro ao processar webhook GitHub", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Erro ao processar payload"));
        }
    }

    /**
     * Push event: extract task codes from commit messages → set to IN_PROGRESS (if PLANNED)
     */
    private List<String> handlePushEvent(JsonNode root) {
        List<String> updated = new ArrayList<>();
        JsonNode commits = root.get("commits");
        if (commits == null || !commits.isArray()) return updated;

        Set<String> taskCodes = new LinkedHashSet<>();
        for (JsonNode commit : commits) {
            String message = commit.path("message").asText("");
            taskCodes.addAll(extractTaskCodes(message));
        }

        for (String code : taskCodes) {
            Optional<Task> taskOpt = taskRepository.findByTaskCode(code);
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                if (task.getStatus() == TaskStatus.PLANNED) {
                    taskService.startTask(task.getId());
                    updated.add(code + " → IN_PROGRESS");
                    log.info("Push: tarefa {} marcada como IN_PROGRESS", code);
                } else {
                    log.info("Push: tarefa {} já em estado {} — sem alteração", code, task.getStatus());
                }
            } else {
                log.warn("Push: código de tarefa {} não encontrado", code);
            }
        }
        return updated;
    }

    /**
     * Pull request event:
     * - opened/reopened → IN_PROGRESS (if PLANNED)
     * - closed + merged → COMPLETED
     */
    private List<String> handlePullRequestEvent(JsonNode root) {
        List<String> updated = new ArrayList<>();
        String action = root.path("action").asText("");
        JsonNode pr = root.get("pull_request");
        if (pr == null) return updated;

        String title = pr.path("title").asText("");
        String body = pr.path("body").asText("");
        boolean merged = pr.path("merged").asBoolean(false);

        Set<String> taskCodes = new LinkedHashSet<>();
        taskCodes.addAll(extractTaskCodes(title));
        taskCodes.addAll(extractTaskCodes(body));

        for (String code : taskCodes) {
            Optional<Task> taskOpt = taskRepository.findByTaskCode(code);
            if (taskOpt.isEmpty()) {
                log.warn("PR: código de tarefa {} não encontrado", code);
                continue;
            }

            Task task = taskOpt.get();

            if ("closed".equals(action) && merged) {
                // PR merged → complete task
                if (task.getStatus() != TaskStatus.COMPLETED) {
                    taskService.completeTask(task.getId(), null);
                    updated.add(code + " → COMPLETED");
                    log.info("PR merged: tarefa {} marcada como COMPLETED", code);
                }
            } else if ("opened".equals(action) || "reopened".equals(action)) {
                // PR opened → start task
                if (task.getStatus() == TaskStatus.PLANNED) {
                    taskService.startTask(task.getId());
                    updated.add(code + " → IN_PROGRESS");
                    log.info("PR opened: tarefa {} marcada como IN_PROGRESS", code);
                }
            }
        }
        return updated;
    }

    /**
     * Extract task codes matching pattern S\d+-\d+ (e.g., S1-03, S2-15)
     */
    Set<String> extractTaskCodes(String text) {
        Set<String> codes = new LinkedHashSet<>();
        if (text == null || text.isBlank()) return codes;
        Matcher matcher = TASK_CODE_PATTERN.matcher(text);
        while (matcher.find()) {
            codes.add(matcher.group());
        }
        return codes;
    }

    /**
     * Verify GitHub HMAC-SHA256 signature.
     * If no secret is configured, skip verification (development mode).
     */
    private boolean verifySignature(String payload, String signature) {
        String secret = webhookConfig.getSecret();
        if (secret == null || secret.isBlank()) {
            log.warn("Webhook secret não configurado — verificação de assinatura desactivada");
            return true;
        }
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + bytesToHex(hash);
            return constantTimeEquals(expected, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Erro ao verificar assinatura HMAC", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
