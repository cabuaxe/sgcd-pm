package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.*;
import ao.gov.sgcd.pm.entity.*;
import ao.gov.sgcd.pm.exception.ResourceNotFoundException;
import ao.gov.sgcd.pm.mapper.TaskExecutionMapper;
import ao.gov.sgcd.pm.mapper.TaskMapper;
import ao.gov.sgcd.pm.mapper.TaskNoteMapper;
import ao.gov.sgcd.pm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskExecutionRepository executionRepository;
    private final SprintRepository sprintRepository;
    private final SprintReportRepository reportRepository;
    private final TaskMapper taskMapper;
    private final TaskNoteMapper noteMapper;
    private final TaskExecutionMapper executionMapper;

    public Page<TaskDTO> findFiltered(Long sprintId, TaskStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return taskRepository.findFiltered(sprintId, status, from, to, pageable)
                .map(taskMapper::toDto);
    }

    public TaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
        return taskMapper.toDto(task);
    }

    public TaskDTO findToday() {
        LocalDate today = LocalDate.now();
        return taskRepository.findBySessionDate(today)
                .map(taskMapper::toDto)
                .orElseGet(() -> {
                    List<Task> upcoming = taskRepository.findUpcomingPlanned(today);
                    if (upcoming.isEmpty()) return null;
                    return taskMapper.toDto(upcoming.get(0));
                });
    }

    public TaskDTO findNext() {
        List<Task> next = taskRepository.findNextPlanned();
        if (next.isEmpty()) return null;
        return taskMapper.toDto(next.get(0));
    }

    @Transactional
    public TaskDTO update(Long id, TaskUpdateDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));

        if (dto.getCompletionNotes() != null) task.setCompletionNotes(dto.getCompletionNotes());
        if (dto.getBlockers() != null) task.setBlockers(dto.getBlockers());
        if (dto.getActualHours() != null) task.setActualHours(dto.getActualHours());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO startTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
        log.info("Iniciando tarefa {} ({})", task.getTaskCode(), id);
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());

        // Activate sprint if needed
        Sprint sprint = task.getSprint();
        if (sprint.getStatus() == SprintStatus.PLANNED) {
            sprint.setStatus(SprintStatus.ACTIVE);
            sprintRepository.save(sprint);
        }

        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO completeTask(Long id, TaskUpdateDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));

        // 1. Mark task as COMPLETED
        log.info("Concluindo tarefa {} ({})", task.getTaskCode(), id);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        // 2. Calculate actualHours
        if (dto != null && dto.getActualHours() != null) {
            task.setActualHours(dto.getActualHours());
        } else {
            task.setActualHours(task.getPlannedHours());
        }

        if (dto != null && dto.getCompletionNotes() != null) {
            task.setCompletionNotes(dto.getCompletionNotes());
        }

        taskRepository.save(task);

        // 3-4. Update sprint metrics
        Sprint sprint = task.getSprint();
        sprint.setCompletedSessions(sprint.getCompletedSessions() + 1);
        sprint.setActualHours(sprint.getActualHours().add(task.getActualHours()));

        // 5. Check if last task of sprint
        int remaining = taskRepository.countBySprintIdAndStatus(sprint.getId(), TaskStatus.PLANNED)
                + taskRepository.countBySprintIdAndStatus(sprint.getId(), TaskStatus.IN_PROGRESS);

        if (remaining == 0) {
            sprint.setStatus(SprintStatus.COMPLETED);
            sprintRepository.save(sprint);

            // 6. Activate next sprint
            sprintRepository.findFirstByStatusOrderBySprintNumberAsc(SprintStatus.PLANNED)
                    .ifPresent(next -> {
                        next.setStatus(SprintStatus.ACTIVE);
                        sprintRepository.save(next);
                    });
        } else {
            sprintRepository.save(sprint);
        }

        return taskMapper.toDto(task);
    }

    @Transactional
    public TaskDTO blockTask(Long id, String reason) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
        log.info("Bloqueando tarefa {} ({}): {}", task.getTaskCode(), id, reason);
        task.setStatus(TaskStatus.BLOCKED);
        task.setBlockers(reason);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO skipTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));
        task.setStatus(TaskStatus.SKIPPED);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskNoteDTO addNote(Long taskId, TaskNoteDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + taskId));

        TaskNote note = TaskNote.builder()
                .task(task)
                .noteType(dto.getNoteType())
                .content(dto.getContent())
                .author(dto.getAuthor())
                .build();

        task.getNotes().add(note);
        taskRepository.save(task);

        return noteMapper.toDto(note);
    }

    public List<TaskDTO> findRecent(int limit) {
        return taskRepository.findRecentCompleted(PageRequest.of(0, limit))
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    public List<TaskExecutionDTO> getExecutions(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + taskId));
        return executionMapper.toDtoList(executionRepository.findByTaskIdOrderByStartedAtDesc(taskId));
    }

    @Transactional
    public TaskExecutionDTO addExecution(Long taskId, TaskExecutionDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + taskId));

        TaskExecution execution = TaskExecution.builder()
                .task(task)
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .hoursSpent(dto.getHoursSpent())
                .promptUsed(dto.getPromptUsed())
                .responseSummary(dto.getResponseSummary())
                .notes(dto.getNotes())
                .build();

        return executionMapper.toDto(executionRepository.save(execution));
    }
}
