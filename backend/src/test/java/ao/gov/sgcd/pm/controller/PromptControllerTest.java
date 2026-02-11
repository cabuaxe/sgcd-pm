package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.PromptDTO;
import ao.gov.sgcd.pm.service.PromptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromptController.class)
@AutoConfigureMockMvc(addFilters = false)
class PromptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private PromptService promptService;

    @Test
    void getTodayPrompt_shouldReturn200WithPrompt() throws Exception {
        PromptDTO prompt = PromptDTO.builder()
                .taskId(42L)
                .taskCode("S2-006")
                .title("Implementar Entity Task")
                .prompt("SGCD Development Session\nSprint 2: Backend Core...")
                .build();

        when(promptService.getTodayPrompt()).thenReturn(prompt);

        mockMvc.perform(get("/v1/prompts/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId", is(42)))
                .andExpect(jsonPath("$.taskCode", is("S2-006")))
                .andExpect(jsonPath("$.title", is("Implementar Entity Task")))
                .andExpect(jsonPath("$.prompt", notNullValue()));
    }

    @Test
    void getTodayPrompt_whenNoTask_shouldReturn200WithMessage() throws Exception {
        PromptDTO noTaskPrompt = PromptDTO.builder()
                .prompt("Nenhuma tarefa encontrada para hoje.")
                .build();

        when(promptService.getTodayPrompt()).thenReturn(noTaskPrompt);

        mockMvc.perform(get("/v1/prompts/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt", is("Nenhuma tarefa encontrada para hoje.")))
                .andExpect(jsonPath("$.taskId").doesNotExist());
    }

    @Test
    void getTaskPrompt_shouldReturn200WithPrompt() throws Exception {
        PromptDTO prompt = PromptDTO.builder()
                .taskId(1L)
                .taskCode("S1-001")
                .title("Configuracao do projecto Maven")
                .prompt("SGCD Development Session\nSprint 1: Fundacao\n...")
                .build();

        when(promptService.getPromptForTask(1L)).thenReturn(prompt);

        mockMvc.perform(get("/v1/prompts/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId", is(1)))
                .andExpect(jsonPath("$.taskCode", is("S1-001")))
                .andExpect(jsonPath("$.title", is("Configuracao do projecto Maven")))
                .andExpect(jsonPath("$.prompt", containsString("SGCD Development Session")));
    }

    @Test
    void getTaskPrompt_whenTaskNotFound_shouldReturn500() throws Exception {
        when(promptService.getPromptForTask(999L))
                .thenThrow(new RuntimeException("Tarefa nao encontrada: 999"));

        mockMvc.perform(get("/v1/prompts/task/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getProjectContext_shouldReturn200WithContextString() throws Exception {
        String context = "SGCD - Sistema de Gestao Consular Digital\nEmbaixada da Republica de Angola...";

        when(promptService.getProjectContext()).thenReturn(context);

        mockMvc.perform(get("/v1/prompts/context"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SGCD")));
    }

    @Test
    void getProjectContext_shouldReturnPlainTextContent() throws Exception {
        String context = "Full project context with stack details";

        when(promptService.getProjectContext()).thenReturn(context);

        mockMvc.perform(get("/v1/prompts/context"))
                .andExpect(status().isOk())
                .andExpect(content().string(is("Full project context with stack details")));
    }
}
