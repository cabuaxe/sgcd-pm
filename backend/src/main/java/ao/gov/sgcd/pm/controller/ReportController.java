package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.ReportDTO;
import ao.gov.sgcd.pm.service.PdfExportService;
import ao.gov.sgcd.pm.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Geração de relatórios de sprint e exportação em PDF")
public class ReportController {

    private final ReportService reportService;
    private final PdfExportService pdfExportService;

    @Operation(summary = "Listar todos os relatórios", description = "Devolve a lista completa de relatórios gerados")
    @ApiResponse(responseCode = "200", description = "Lista de relatórios devolvida com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping
    public ResponseEntity<List<ReportDTO>> findAll() {
        return ResponseEntity.ok(reportService.findAll());
    }

    @Operation(summary = "Obter relatório por sprint", description = "Devolve o relatório associado a um sprint específico")
    @ApiResponse(responseCode = "200", description = "Relatório encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Relatório não encontrado para o sprint indicado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<ReportDTO> findBySprintId(@PathVariable Long sprintId) {
        return ResponseEntity.ok(reportService.findBySprintId(sprintId));
    }

    @Operation(summary = "Gerar relatório do sprint", description = "Gera automaticamente um relatório para o sprint indicado")
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    @ApiResponse(responseCode = "404", description = "Sprint não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @PostMapping("/sprint/{sprintId}/generate")
    public ResponseEntity<ReportDTO> generateReport(@PathVariable Long sprintId) {
        return ResponseEntity.ok(reportService.generateReport(sprintId));
    }

    @Operation(summary = "Descarregar relatório em PDF", description = "Exporta o relatório indicado em formato PDF para download")
    @ApiResponse(responseCode = "200", description = "PDF gerado e devolvido com sucesso")
    @ApiResponse(responseCode = "404", description = "Relatório não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = reportService.generatePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
