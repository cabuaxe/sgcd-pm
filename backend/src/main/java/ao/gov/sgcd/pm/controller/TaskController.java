package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.TaskDTO;
import ao.gov.sgcd.pm.dto.TaskExecutionDTO;
import ao.gov.sgcd.pm.dto.TaskNoteDTO;
import ao.gov.sgcd.pm.dto.TaskUpdateDTO;
import ao.gov.sgcd.pm.dto.PromptDTO;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.service.TaskService;
import ao.gov.sgcd.pm.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final PromptService promptService;

    @GetMapping
    public ResponseEntity<Page<TaskDTO>> findAll(
            @RequestParam(required = false) Long sprint,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taskService.findFiltered(sprint, status, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @GetMapping("/today")
    public ResponseEntity<TaskDTO> findToday() {
        TaskDTO task = taskService.findToday();
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.noContent().build();
    }

    @GetMapping("/next")
    public ResponseEntity<TaskDTO> findNext() {
        TaskDTO task = taskService.findNext();
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @RequestBody TaskUpdateDTO dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TaskDTO> start(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.startTask(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TaskDTO> complete(@PathVariable Long id,
                                            @RequestBody(required = false) TaskUpdateDTO dto) {
        return ResponseEntity.ok(taskService.completeTask(id, dto));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<TaskDTO> block(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(taskService.blockTask(id, body.get("reason")));
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<TaskDTO> skip(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.skipTask(id));
    }

    @GetMapping("/{id}/prompt")
    public ResponseEntity<PromptDTO> getPrompt(@PathVariable Long id) {
        return ResponseEntity.ok(promptService.getPromptForTask(id));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<TaskNoteDTO> addNote(@PathVariable Long id, @RequestBody TaskNoteDTO dto) {
        return ResponseEntity.ok(taskService.addNote(id, dto));
    }

    @GetMapping("/{id}/executions")
    public ResponseEntity<List<TaskExecutionDTO>> getExecutions(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getExecutions(id));
    }

    @PostMapping("/{id}/executions")
    public ResponseEntity<TaskExecutionDTO> addExecution(@PathVariable Long id,
                                                         @RequestBody TaskExecutionDTO dto) {
        return ResponseEntity.ok(taskService.addExecution(id, dto));
    }
}
