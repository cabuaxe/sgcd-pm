package ao.gov.sgcd.pm.repository;

import ao.gov.sgcd.pm.entity.Task;
import ao.gov.sgcd.pm.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByTaskCode(String taskCode);

    List<Task> findBySprintIdOrderBySortOrderAsc(Long sprintId);

    @Query("SELECT t FROM Task t WHERE t.sprint.id = :sprintId AND t.status = :status ORDER BY t.sortOrder ASC")
    List<Task> findBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.sessionDate = :date")
    Optional<Task> findBySessionDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.sessionDate >= :date AND t.status = 'PLANNED' ORDER BY t.sessionDate ASC, t.sortOrder ASC")
    List<Task> findUpcomingPlanned(@Param("date") LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.status = 'PLANNED' ORDER BY t.sessionDate ASC, t.sortOrder ASC")
    List<Task> findNextPlanned();

    @Query("SELECT t FROM Task t ORDER BY t.completedAt DESC, t.sessionDate DESC")
    List<Task> findRecentCompleted(Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.sessionDate BETWEEN :from AND :to ORDER BY t.sessionDate ASC, t.sortOrder ASC")
    List<Task> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT t FROM Task t WHERE (:sprintId IS NULL OR t.sprint.id = :sprintId) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:from IS NULL OR t.sessionDate >= :from) " +
           "AND (:to IS NULL OR t.sessionDate <= :to) " +
           "ORDER BY t.sessionDate ASC, t.sortOrder ASC")
    Page<Task> findFiltered(@Param("sprintId") Long sprintId,
                            @Param("status") TaskStatus status,
                            @Param("from") LocalDate from,
                            @Param("to") LocalDate to,
                            Pageable pageable);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprint.id = :sprintId AND t.status = :status")
    Integer countBySprintIdAndStatus(@Param("sprintId") Long sprintId, @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Integer countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COALESCE(SUM(t.actualHours), 0) FROM Task t WHERE t.status = 'COMPLETED'")
    java.math.BigDecimal sumActualHoursCompleted();

    @Query("SELECT t FROM Task t WHERE t.sessionDate BETWEEN :weekStart AND :weekEnd ORDER BY t.sessionDate ASC")
    List<Task> findByWeek(@Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.sprint.id = :sprintId")
    Integer countBySprintId(@Param("sprintId") Long sprintId);

    @Query("SELECT t.sprint.id, t.status, COUNT(t) FROM Task t GROUP BY t.sprint.id, t.status")
    List<Object[]> countGroupedBySprintAndStatus();

    @Query("SELECT COALESCE(SUM(t.plannedHours), 0) FROM Task t")
    BigDecimal sumAllPlannedHours();
}
