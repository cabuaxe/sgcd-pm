package ao.gov.sgcd.pm.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProjectProgressDTO {

    // Overall
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer totalHoursPlanned;
    private BigDecimal totalHoursSpent;
    private Double overallProgress;
    private Long daysRemaining;
    private LocalDate startDate;
    private LocalDate targetDate;

    // Task status totals
    private Integer totalPlanned;
    private Integer totalInProgress;
    private Integer totalCompleted;
    private Integer totalBlocked;
    private Integer totalSkipped;

    // Velocity
    private Double avgSessionsPerWeek;
    private Double avgHoursPerWeek;
    private Long weeksElapsed;
    private Long weeksRemaining;

    // Per-sprint breakdown
    private List<SprintProgressDTO> sprints;

    @Data
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class SprintProgressDTO {
        private Integer sprintNumber;
        private String name;
        private String status;
        private String color;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalSessions;
        private Integer completedSessions;
        private Integer totalHours;
        private BigDecimal actualHours;
        private Double progress;
        private Integer plannedTasks;
        private Integer inProgressTasks;
        private Integer completedTasks;
        private Integer blockedTasks;
        private Integer skippedTasks;
    }
}
