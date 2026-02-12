package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.SprintDTO;
import ao.gov.sgcd.pm.dto.SprintProgressDTO;
import ao.gov.sgcd.pm.entity.SprintStatus;
import ao.gov.sgcd.pm.exception.ResourceNotFoundException;
import ao.gov.sgcd.pm.service.SprintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SprintService sprintService;

    @Autowired
    private ObjectMapper objectMapper;

    private SprintDTO buildSampleSprint() {
        return SprintDTO.builder()
                .id(1L)
                .sprintNumber(1)
                .name("Sprint 1 - Fundacao")
                .nameEn("Sprint 1 - Foundation")
                .description("Foundation sprint")
                .weeks(4)
                .totalHours(120)
                .totalSessions(36)
                .startDate(LocalDate.of(2026, 3, 2))
                .endDate(LocalDate.of(2026, 4, 13))
                .focus("Backend foundation")
                .color("#CC092F")
                .status(SprintStatus.ACTIVE)
                .actualHours(BigDecimal.valueOf(10.5))
                .completedSessions(5)
                .taskCount(36)
                .progressPercent(13.89)
                .build();
    }

    @Test
    void findAll_shouldReturn200WithSprintList() throws Exception {
        SprintDTO sprint1 = buildSampleSprint();
        SprintDTO sprint2 = SprintDTO.builder()
                .id(2L)
                .sprintNumber(2)
                .name("Sprint 2 - Backend Core")
                .status(SprintStatus.PLANNED)
                .build();

        when(sprintService.findAll()).thenReturn(List.of(sprint1, sprint2));

        mockMvc.perform(get("/v1/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].sprintNumber", is(1)))
                .andExpect(jsonPath("$[0].name", is("Sprint 1 - Fundacao")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].sprintNumber", is(2)));
    }

    @Test
    void findAll_withEmptyList_shouldReturn200WithEmptyArray() throws Exception {
        when(sprintService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/v1/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void findById_shouldReturn200WithSprint() throws Exception {
        SprintDTO sprint = buildSampleSprint();
        when(sprintService.findById(1L)).thenReturn(sprint);

        mockMvc.perform(get("/v1/sprints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.sprintNumber", is(1)))
                .andExpect(jsonPath("$.name", is("Sprint 1 - Fundacao")))
                .andExpect(jsonPath("$.nameEn", is("Sprint 1 - Foundation")))
                .andExpect(jsonPath("$.weeks", is(4)))
                .andExpect(jsonPath("$.totalHours", is(120)))
                .andExpect(jsonPath("$.totalSessions", is(36)))
                .andExpect(jsonPath("$.focus", is("Backend foundation")))
                .andExpect(jsonPath("$.color", is("#CC092F")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.completedSessions", is(5)))
                .andExpect(jsonPath("$.taskCount", is(36)));
    }

    @Test
    void findById_whenNotFound_shouldReturn404() throws Exception {
        when(sprintService.findById(999L)).thenThrow(new ResourceNotFoundException("Sprint não encontrado: 999"));

        mockMvc.perform(get("/v1/sprints/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProgress_shouldReturn200WithProgress() throws Exception {
        SprintProgressDTO progress = SprintProgressDTO.builder()
                .sprintNumber(1)
                .name("Sprint 1 - Fundacao")
                .totalSessions(36)
                .completedSessions(5)
                .totalHours(120)
                .actualHours(BigDecimal.valueOf(17.5))
                .progressPercent(13.89)
                .plannedTasks(28)
                .inProgressTasks(1)
                .completedTasks(5)
                .blockedTasks(1)
                .skippedTasks(1)
                .build();

        when(sprintService.getProgress(1L)).thenReturn(progress);

        mockMvc.perform(get("/v1/sprints/1/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sprintNumber", is(1)))
                .andExpect(jsonPath("$.name", is("Sprint 1 - Fundacao")))
                .andExpect(jsonPath("$.totalSessions", is(36)))
                .andExpect(jsonPath("$.completedSessions", is(5)))
                .andExpect(jsonPath("$.totalHours", is(120)))
                .andExpect(jsonPath("$.progressPercent", is(13.89)))
                .andExpect(jsonPath("$.plannedTasks", is(28)))
                .andExpect(jsonPath("$.inProgressTasks", is(1)))
                .andExpect(jsonPath("$.completedTasks", is(5)))
                .andExpect(jsonPath("$.blockedTasks", is(1)))
                .andExpect(jsonPath("$.skippedTasks", is(1)));
    }

    @Test
    void findActive_shouldReturn200WithActiveSprint() throws Exception {
        SprintDTO activeSprint = buildSampleSprint();
        when(sprintService.findActive()).thenReturn(activeSprint);

        mockMvc.perform(get("/v1/sprints/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void findActive_whenNoActive_shouldReturn404() throws Exception {
        when(sprintService.findActive()).thenThrow(new ResourceNotFoundException("Nenhum sprint activo ou planeado"));

        mockMvc.perform(get("/v1/sprints/active"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn200WithUpdatedSprint() throws Exception {
        SprintDTO updateDto = SprintDTO.builder()
                .status(SprintStatus.ACTIVE)
                .completionNotes("Sprint started")
                .build();

        SprintDTO updatedSprint = buildSampleSprint();
        updatedSprint.setCompletionNotes("Sprint started");

        when(sprintService.update(eq(1L), any(SprintDTO.class))).thenReturn(updatedSprint);

        mockMvc.perform(patch("/v1/sprints/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.completionNotes", is("Sprint started")));
    }

    @Test
    void update_withStatusChange_shouldReturn200() throws Exception {
        SprintDTO updateDto = SprintDTO.builder()
                .status(SprintStatus.COMPLETED)
                .build();

        SprintDTO updatedSprint = buildSampleSprint();
        updatedSprint.setStatus(SprintStatus.COMPLETED);

        when(sprintService.update(eq(1L), any(SprintDTO.class))).thenReturn(updatedSprint);

        mockMvc.perform(patch("/v1/sprints/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void getProgress_whenNotFound_shouldReturn404() throws Exception {
        when(sprintService.getProgress(999L)).thenThrow(new ResourceNotFoundException("Sprint não encontrado: 999"));

        mockMvc.perform(get("/v1/sprints/999/progress"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        SprintDTO updateDto = SprintDTO.builder()
                .status(SprintStatus.ACTIVE)
                .build();

        when(sprintService.update(eq(999L), any(SprintDTO.class)))
                .thenThrow(new ResourceNotFoundException("Sprint não encontrado: 999"));

        mockMvc.perform(patch("/v1/sprints/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }
}
