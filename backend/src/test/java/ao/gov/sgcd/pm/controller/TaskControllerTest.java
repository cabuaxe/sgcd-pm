package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.dto.PromptDTO;
import ao.gov.sgcd.pm.dto.TaskDTO;
import ao.gov.sgcd.pm.dto.TaskNoteDTO;
import ao.gov.sgcd.pm.dto.TaskUpdateDTO;
import ao.gov.sgcd.pm.entity.NoteType;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.service.PromptService;
import ao.gov.sgcd.pm.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private PromptService promptService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskDTO buildSampleTask() {
        return TaskDTO.builder()
                .id(1L)
                .sprintId(1L)
                .sprintNumber(1)
                .sprintName("Sprint 1 - Fundacao")
                .taskCode("S1-001")
                .sessionDate(LocalDate.of(2026, 3, 2))
                .dayOfWeek("SEG")
                .weekNumber(1)
                .plannedHours(BigDecimal.valueOf(3.5))
                .title("Configuracao do projecto Maven")
                .description("Setup do projecto Spring Boot")
                .deliverables(List.of("pom.xml", "application.yml"))
                .validationCriteria(List.of("mvn clean compile", "App starts"))
                .coverageTarget("N/A")
                .status(TaskStatus.PLANNED)
                .sortOrder(1)
                .build();
    }

    @Test
    void findAll_shouldReturn200WithPageOfTasks() throws Exception {
        TaskDTO task = buildSampleTask();
        Page<TaskDTO> page = new PageImpl<>(List.of(task), PageRequest.of(0, 20), 1);

        when(taskService.findFiltered(isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].taskCode", is("S1-001")))
                .andExpect(jsonPath("$.content[0].title", is("Configuracao do projecto Maven")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void findAll_withSprintFilter_shouldReturn200() throws Exception {
        TaskDTO task = buildSampleTask();
        Page<TaskDTO> page = new PageImpl<>(List.of(task), PageRequest.of(0, 20), 1);

        when(taskService.findFiltered(eq(1L), isNull(), isNull(), isNull(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/v1/tasks")
                        .param("sprint", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void findAll_emptyResult_shouldReturn200WithEmptyPage() throws Exception {
        Page<TaskDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(taskService.findFiltered(isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void findById_shouldReturn200WithTask() throws Exception {
        TaskDTO task = buildSampleTask();
        when(taskService.findById(1L)).thenReturn(task);

        mockMvc.perform(get("/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.taskCode", is("S1-001")))
                .andExpect(jsonPath("$.title", is("Configuracao do projecto Maven")))
                .andExpect(jsonPath("$.status", is("PLANNED")))
                .andExpect(jsonPath("$.sprintNumber", is(1)));
    }

    @Test
    void findById_whenNotFound_shouldReturn500() throws Exception {
        when(taskService.findById(999L)).thenThrow(new RuntimeException("Tarefa nao encontrada: 999"));

        mockMvc.perform(get("/v1/tasks/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void findToday_whenTaskExists_shouldReturn200() throws Exception {
        TaskDTO task = buildSampleTask();
        when(taskService.findToday()).thenReturn(task);

        mockMvc.perform(get("/v1/tasks/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.taskCode", is("S1-001")));
    }

    @Test
    void findToday_whenNoTask_shouldReturn204() throws Exception {
        when(taskService.findToday()).thenReturn(null);

        mockMvc.perform(get("/v1/tasks/today"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findNext_whenTaskExists_shouldReturn200() throws Exception {
        TaskDTO task = buildSampleTask();
        task.setStatus(TaskStatus.PLANNED);
        when(taskService.findNext()).thenReturn(task);

        mockMvc.perform(get("/v1/tasks/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void findNext_whenNoTask_shouldReturn204() throws Exception {
        when(taskService.findNext()).thenReturn(null);

        mockMvc.perform(get("/v1/tasks/next"))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_shouldReturn200WithUpdatedTask() throws Exception {
        TaskUpdateDTO updateDto = TaskUpdateDTO.builder()
                .description("Updated description")
                .actualHours(BigDecimal.valueOf(3.0))
                .build();

        TaskDTO updatedTask = buildSampleTask();
        updatedTask.setDescription("Updated description");
        updatedTask.setActualHours(BigDecimal.valueOf(3.0));

        when(taskService.update(eq(1L), any(TaskUpdateDTO.class))).thenReturn(updatedTask);

        mockMvc.perform(patch("/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void start_shouldReturn200WithStartedTask() throws Exception {
        TaskDTO startedTask = buildSampleTask();
        startedTask.setStatus(TaskStatus.IN_PROGRESS);
        startedTask.setStartedAt(LocalDateTime.now());

        when(taskService.startTask(1L)).thenReturn(startedTask);

        mockMvc.perform(post("/v1/tasks/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.startedAt", notNullValue()));
    }

    @Test
    void complete_shouldReturn200WithCompletedTask() throws Exception {
        TaskUpdateDTO dto = TaskUpdateDTO.builder()
                .actualHours(BigDecimal.valueOf(3.5))
                .completionNotes("Task done")
                .build();

        TaskDTO completedTask = buildSampleTask();
        completedTask.setStatus(TaskStatus.COMPLETED);
        completedTask.setCompletedAt(LocalDateTime.now());
        completedTask.setActualHours(BigDecimal.valueOf(3.5));
        completedTask.setCompletionNotes("Task done");

        when(taskService.completeTask(eq(1L), any(TaskUpdateDTO.class))).thenReturn(completedTask);

        mockMvc.perform(post("/v1/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.completedAt", notNullValue()))
                .andExpect(jsonPath("$.completionNotes", is("Task done")));
    }

    @Test
    void complete_withoutBody_shouldReturn200() throws Exception {
        TaskDTO completedTask = buildSampleTask();
        completedTask.setStatus(TaskStatus.COMPLETED);

        when(taskService.completeTask(eq(1L), isNull())).thenReturn(completedTask);

        mockMvc.perform(post("/v1/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void block_shouldReturn200WithBlockedTask() throws Exception {
        TaskDTO blockedTask = buildSampleTask();
        blockedTask.setStatus(TaskStatus.BLOCKED);
        blockedTask.setBlockers("Waiting for DB access");

        when(taskService.blockTask(1L, "Waiting for DB access")).thenReturn(blockedTask);

        mockMvc.perform(post("/v1/tasks/1/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Waiting for DB access"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("BLOCKED")))
                .andExpect(jsonPath("$.blockers", is("Waiting for DB access")));
    }

    @Test
    void skip_shouldReturn200WithSkippedTask() throws Exception {
        TaskDTO skippedTask = buildSampleTask();
        skippedTask.setStatus(TaskStatus.SKIPPED);

        when(taskService.skipTask(1L)).thenReturn(skippedTask);

        mockMvc.perform(post("/v1/tasks/1/skip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SKIPPED")));
    }

    @Test
    void getPrompt_shouldReturn200WithPrompt() throws Exception {
        PromptDTO prompt = PromptDTO.builder()
                .taskId(1L)
                .taskCode("S1-001")
                .title("Configuracao do projecto Maven")
                .prompt("SGCD Development Session prompt content...")
                .build();

        when(promptService.getPromptForTask(1L)).thenReturn(prompt);

        mockMvc.perform(get("/v1/tasks/1/prompt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId", is(1)))
                .andExpect(jsonPath("$.taskCode", is("S1-001")))
                .andExpect(jsonPath("$.title", is("Configuracao do projecto Maven")))
                .andExpect(jsonPath("$.prompt", notNullValue()));
    }

    @Test
    void addNote_shouldReturn200WithCreatedNote() throws Exception {
        TaskNoteDTO noteDto = TaskNoteDTO.builder()
                .noteType(NoteType.OBSERVATION)
                .content("Important note about this task")
                .author("admin")
                .build();

        TaskNoteDTO createdNote = TaskNoteDTO.builder()
                .id(1L)
                .taskId(1L)
                .noteType(NoteType.OBSERVATION)
                .content("Important note about this task")
                .author("admin")
                .createdAt(LocalDateTime.now())
                .build();

        when(taskService.addNote(eq(1L), any(TaskNoteDTO.class))).thenReturn(createdNote);

        mockMvc.perform(post("/v1/tasks/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.taskId", is(1)))
                .andExpect(jsonPath("$.noteType", is("OBSERVATION")))
                .andExpect(jsonPath("$.content", is("Important note about this task")))
                .andExpect(jsonPath("$.author", is("admin")));
    }
}
