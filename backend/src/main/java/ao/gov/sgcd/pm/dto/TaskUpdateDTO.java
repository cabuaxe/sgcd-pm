package ao.gov.sgcd.pm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskUpdateDTO {
    @Size(max = 2000, message = "Notas de conclusão não podem exceder 2000 caracteres")
    private String completionNotes;

    @Size(max = 500, message = "Bloqueios não podem exceder 500 caracteres")
    private String blockers;

    @DecimalMin(value = "0.0", message = "Horas devem ser >= 0")
    @DecimalMax(value = "24.0", message = "Horas devem ser <= 24")
    private BigDecimal actualHours;

    @Size(max = 2000, message = "Descrição não pode exceder 2000 caracteres")
    private String description;
}
