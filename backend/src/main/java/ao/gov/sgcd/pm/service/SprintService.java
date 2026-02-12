package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.dto.SprintDTO;
import ao.gov.sgcd.pm.dto.SprintProgressDTO;
import ao.gov.sgcd.pm.entity.Sprint;
import ao.gov.sgcd.pm.entity.SprintStatus;
import ao.gov.sgcd.pm.entity.TaskStatus;
import ao.gov.sgcd.pm.exception.ResourceNotFoundException;
import ao.gov.sgcd.pm.mapper.SprintMapper;
import ao.gov.sgcd.pm.repository.SprintRepository;
import ao.gov.sgcd.pm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final SprintMapper sprintMapper;

    public List<SprintDTO> findAll() {
        return sprintRepository.findAllOrdered().stream()
                .map(this::enrichDto)
                .toList();
    }

    public SprintDTO findById(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint não encontrado: " + id));
        return enrichDto(sprint);
    }

    public SprintDTO findActive() {
        Sprint sprint = sprintRepository.findActiveSprint()
                .orElseGet(() -> sprintRepository.findFirstByStatusOrderBySprintNumberAsc(SprintStatus.PLANNED)
                        .orElseThrow(() -> new ResourceNotFoundException("Nenhum sprint activo ou planeado")));
        return enrichDto(sprint);
    }

    public SprintProgressDTO getProgress(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint não encontrado: " + id));

        int planned = taskRepository.countBySprintIdAndStatus(id, TaskStatus.PLANNED);
        int inProgress = taskRepository.countBySprintIdAndStatus(id, TaskStatus.IN_PROGRESS);
        int completed = taskRepository.countBySprintIdAndStatus(id, TaskStatus.COMPLETED);
        int blocked = taskRepository.countBySprintIdAndStatus(id, TaskStatus.BLOCKED);
        int skipped = taskRepository.countBySprintIdAndStatus(id, TaskStatus.SKIPPED);

        double progress = sprint.getTotalSessions() > 0
                ? (completed * 100.0) / sprint.getTotalSessions()
                : 0;

        return SprintProgressDTO.builder()
                .sprintNumber(sprint.getSprintNumber())
                .name(sprint.getName())
                .totalSessions(sprint.getTotalSessions())
                .completedSessions(sprint.getCompletedSessions())
                .totalHours(sprint.getTotalHours())
                .actualHours(sprint.getActualHours())
                .progressPercent(progress)
                .plannedTasks(planned)
                .inProgressTasks(inProgress)
                .completedTasks(completed)
                .blockedTasks(blocked)
                .skippedTasks(skipped)
                .build();
    }

    @Transactional
    public SprintDTO update(Long id, SprintDTO dto) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint não encontrado: " + id));

        if (dto.getStatus() != null) sprint.setStatus(dto.getStatus());
        if (dto.getCompletionNotes() != null) sprint.setCompletionNotes(dto.getCompletionNotes());

        return enrichDto(sprintRepository.save(sprint));
    }

    private SprintDTO enrichDto(Sprint sprint) {
        SprintDTO dto = sprintMapper.toDto(sprint);
        dto.setTaskCount(taskRepository.countBySprintId(sprint.getId()));
        double progress = sprint.getTotalSessions() > 0
                ? (sprint.getCompletedSessions() * 100.0) / sprint.getTotalSessions()
                : 0;
        dto.setProgressPercent(progress);
        return dto;
    }
}
