package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.dto.BlockedDayDTO;
import ao.gov.sgcd.pm.dto.CalendarDTO;
import ao.gov.sgcd.pm.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendário", description = "Visualização do calendário de sessões e dias bloqueados")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "Obter calendário", description = "Devolve o calendário de sessões para um mês e ano específicos (ou o mês actual por omissão)")
    @ApiResponse(responseCode = "200", description = "Calendário devolvido com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping
    public ResponseEntity<CalendarDTO> getCalendar(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        return ResponseEntity.ok(calendarService.getCalendar(y, m));
    }

    @Operation(summary = "Listar dias bloqueados", description = "Devolve a lista de todos os dias bloqueados (feriados, indisponibilidades)")
    @ApiResponse(responseCode = "200", description = "Lista de dias bloqueados devolvida com sucesso")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    @GetMapping("/blocked")
    public ResponseEntity<List<BlockedDayDTO>> getBlockedDays() {
        return ResponseEntity.ok(calendarService.getBlockedDays());
    }
}
