package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.SprintDTO;
import ao.gov.sgcd.pm.dto.SprintProgressDTO;
import ao.gov.sgcd.pm.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Gestão de sprints: listar, consultar, progresso e actualizar")
public class SprintController {

    private final SprintService sprintService;

    @Operation(summary = "Listar todos os sprints", description = "Devolve a lista completa de sprints do projecto")
    @ApiResponse(responseCode = "200", description = "Lista de sprints devolvida com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping
    public ResponseEntity<List<SprintDTO>> findAll() {
        return ResponseEntity.ok(sprintService.findAll());
    }

    @Operation(summary = "Obter sprint por ID", description = "Devolve os detalhes de um sprint específico")
    @ApiResponse(responseCode = "200", description = "Sprint encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Sprint não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/{id}")
    public ResponseEntity<SprintDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.findById(id));
    }

    @Operation(summary = "Obter progresso do sprint", description = "Devolve as métricas de progresso de um sprint específico")
    @ApiResponse(responseCode = "200", description = "Progresso do sprint devolvido com sucesso")
    @ApiResponse(responseCode = "404", description = "Sprint não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/{id}/progress")
    public ResponseEntity<SprintProgressDTO> getProgress(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.getProgress(id));
    }

    @Operation(summary = "Obter sprint activo", description = "Devolve o sprint actualmente em execução")
    @ApiResponse(responseCode = "200", description = "Sprint activo devolvido com sucesso")
    @ApiResponse(responseCode = "404", description = "Nenhum sprint activo encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/active")
    public ResponseEntity<SprintDTO> findActive() {
        return ResponseEntity.ok(sprintService.findActive());
    }

    @Operation(summary = "Actualizar sprint", description = "Actualiza parcialmente os dados de um sprint existente")
    @ApiResponse(responseCode = "200", description = "Sprint actualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Sprint não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PatchMapping("/{id}")
    public ResponseEntity<SprintDTO> update(@PathVariable Long id, @RequestBody SprintDTO dto) {
        return ResponseEntity.ok(sprintService.update(id, dto));
    }
}
