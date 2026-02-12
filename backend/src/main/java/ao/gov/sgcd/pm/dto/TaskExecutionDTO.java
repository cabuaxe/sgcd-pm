package ao.gov.sgcd.pm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskExecutionDTO {
    private Long id;
    private Long taskId;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @DecimalMin(value = "0.0", message = "Horas devem ser >= 0")
    @DecimalMax(value = "24.0", message = "Horas devem ser <= 24")
    private BigDecimal hoursSpent;

    @Size(max = 5000, message = "Prompt não pode exceder 5000 caracteres")
    private String promptUsed;

    @Size(max = 5000, message = "Resumo da resposta não pode exceder 5000 caracteres")
    private String responseSummary;

    @Size(max = 2000, message = "Notas não podem exceder 2000 caracteres")
    private String notes;
}
