package ao.gov.sgcd.pm.dto;

import ao.gov.sgcd.pm.entity.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskNoteDTO {
    private Long id;
    private Long taskId;

    @NotNull(message = "Tipo de nota é obrigatório")
    private NoteType noteType;

    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(max = 2000, message = "Conteúdo não pode exceder 2000 caracteres")
    private String content;

    @Size(max = 100, message = "Autor não pode exceder 100 caracteres")
    private String author;

    private LocalDateTime createdAt;
}
