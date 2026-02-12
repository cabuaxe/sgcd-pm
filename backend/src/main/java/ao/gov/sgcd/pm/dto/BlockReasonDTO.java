package ao.gov.sgcd.pm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BlockReasonDTO {
    @NotBlank(message = "Motivo de bloqueio é obrigatório")
    @Size(max = 500, message = "Motivo de bloqueio não pode exceder 500 caracteres")
    private String reason;
}
