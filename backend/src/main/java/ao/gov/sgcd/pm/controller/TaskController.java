package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.*;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.service.TaskService;
import ao.gov.sgcd.pm.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Gestão de tarefas: CRUD, iniciar, concluir, bloquear, saltar, notas e execuções")
public class TaskController {

    private final TaskService taskService;
    private final PromptService promptService;

    @Operation(summary = "Listar tarefas", description = "Devolve uma lista paginada de tarefas com filtros opcionais por sprint, estado e intervalo de datas")
    @ApiResponse(responseCode = "200", description = "Lista de tarefas devolvida com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> findAll(
            @RequestParam(required = false) Long sprint,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(taskService.findFiltered(sprint, status, from, to, pageable));
    }

    @Operation(summary = "Obter tarefa por ID", description = "Devolve os detalhes de uma tarefa específica")
    @ApiResponse(responseCode = "200", description = "Tarefa encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @Operation(summary = "Obter tarefa de hoje", description = "Devolve a tarefa agendada para a sessão de hoje")
    @ApiResponse(responseCode = "200", description = "Tarefa de hoje devolvida com sucesso")
    @ApiResponse(responseCode = "204", description = "Não há tarefa agendada para hoje")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/today")
    public ResponseEntity<TaskDTO> findToday() {
        TaskDTO task = taskService.findToday();
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obter próxima tarefa", description = "Devolve a próxima tarefa pendente a ser realizada")
    @ApiResponse(responseCode = "200", description = "Próxima tarefa devolvida com sucesso")
    @ApiResponse(responseCode = "204", description = "Não há próxima tarefa pendente")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/next")
    public ResponseEntity<TaskDTO> findNext() {
        TaskDTO task = taskService.findNext();
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualizar tarefa", description = "Actualiza parcialmente os dados de uma tarefa existente")
    @ApiResponse(responseCode = "200", description = "Tarefa actualizada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @Operation(summary = "Iniciar tarefa", description = "Marca uma tarefa como em progresso e regista o início da execução")
    @ApiResponse(responseCode = "200", description = "Tarefa iniciada com sucesso")
    @ApiResponse(responseCode = "400", description = "Tarefa não pode ser iniciada no estado actual")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/{id}/start")
    public ResponseEntity<TaskDTO> start(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.startTask(id));
    }

    @Operation(summary = "Concluir tarefa", description = "Marca uma tarefa como concluída, opcionalmente com dados adicionais")
    @ApiResponse(responseCode = "200", description = "Tarefa concluída com sucesso")
    @ApiResponse(responseCode = "400", description = "Tarefa não pode ser concluída no estado actual")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/{id}/complete")
    public ResponseEntity<TaskDTO> complete(@PathVariable Long id,
                                            @Valid @RequestBody(required = false) TaskUpdateDTO dto) {
        return ResponseEntity.ok(taskService.completeTask(id, dto));
    }

    @Operation(summary = "Bloquear tarefa", description = "Marca uma tarefa como bloqueada com motivo obrigatório")
    @ApiResponse(responseCode = "200", description = "Tarefa bloqueada com sucesso")
    @ApiResponse(responseCode = "400", description = "Motivo de bloqueio não fornecido ou dados inválidos")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/{id}/block")
    public ResponseEntity<TaskDTO> block(@PathVariable Long id, @Valid @RequestBody BlockReasonDTO body) {
        return ResponseEntity.ok(taskService.blockTask(id, body.getReason()));
    }

    @Operation(summary = "Saltar tarefa", description = "Marca uma tarefa como saltada, passando para a próxima")
    @ApiResponse(responseCode = "200", description = "Tarefa saltada com sucesso")
    @ApiResponse(responseCode = "400", description = "Tarefa não pode ser saltada no estado actual")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/{id}/skip")
    public ResponseEntity<TaskDTO> skip(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.skipTask(id));
    }

    @Operation(summary = "Obter prompt da tarefa", description = "Gera o prompt do Claude para uma tarefa específica")
    @ApiResponse(responseCode = "200", description = "Prompt gerado com sucesso")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/{id}/prompt")
    public ResponseEntity<PromptDTO> getPrompt(@PathVariable Long id) {
        return ResponseEntity.ok(promptService.getPromptForTask(id));
    }

    @Operation(summary = "Adicionar nota à tarefa", description = "Adiciona uma nota ou comentário a uma tarefa existente")
    @ApiResponse(responseCode = "200", description = "Nota adicionada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da nota inválidos")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/{id}/notes")
    public ResponseEntity<TaskNoteDTO> addNote(@PathVariable Long id, @Valid @RequestBody TaskNoteDTO dto) {
        return ResponseEntity.ok(taskService.addNote(id, dto));
    }

    @Operation(summary = "Listar execuções da tarefa", description = "Devolve o histórico de execuções de uma tarefa")
    @ApiResponse(responseCode = "200", description = "Lista de execuções devolvida com sucesso")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<TaskExecutionDTO>> getExecutions(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getExecutions(id));
    }

    @Operation(summary = "Registar execução da tarefa", description = "Adiciona um novo registo de execução a uma tarefa")
    @ApiResponse(responseCode = "200", description = "Execução registada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados da execução inválidos")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/{id}/executions")
    public ResponseEntity<TaskExecutionDTO> addExecution(@PathVariable Long id,
                                                         @Valid @RequestBody TaskExecutionDTO dto) {
        return ResponseEntity.ok(taskService.addExecution(id, dto));
    }
}
