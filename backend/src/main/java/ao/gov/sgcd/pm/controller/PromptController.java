package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.PromptDTO;
import ao.gov.sgcd.pm.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/prompts")
@RequiredArgsConstructor
@Tag(name = "Prompts", description = "Geração de prompts para o Claude com contexto do projecto")
public class PromptController {

    private final PromptService promptService;

    @Operation(summary = "Obter prompt de hoje", description = "Gera o prompt do Claude para a tarefa agendada para hoje")
    @ApiResponse(responseCode = "200", description = "Prompt de hoje devolvido com sucesso")
    @ApiResponse(responseCode = "404", description = "Nenhuma tarefa agendada para hoje")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/today")
    public ResponseEntity<PromptDTO> getTodayPrompt() {
        return ResponseEntity.ok(promptService.getTodayPrompt());
    }

    @Operation(summary = "Obter prompt por tarefa", description = "Gera o prompt do Claude para uma tarefa específica pelo seu ID")
    @ApiResponse(responseCode = "200", description = "Prompt gerado com sucesso")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/task/{taskId}")
    public ResponseEntity<PromptDTO> getTaskPrompt(@PathVariable Long taskId) {
        return ResponseEntity.ok(promptService.getPromptForTask(taskId));
    }

    @Operation(summary = "Obter contexto do projecto", description = "Devolve o contexto completo do projecto para uso em prompts")
    @ApiResponse(responseCode = "200", description = "Contexto do projecto devolvido com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/context")
    public ResponseEntity<String> getProjectContext() {
        return ResponseEntity.ok(promptService.getProjectContext());
    }
}
