package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.ProjectTask;
import com.erp.brazil.manufacturing.domain.model.ProjectTask.TaskStatus;
import com.erp.brazil.manufacturing.domain.model.ProjectTask.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, UUID> {

    List<ProjectTask> findByProjectId(UUID projectId);

    List<ProjectTask> findByProjectIdAndParentTaskIdIsNull(UUID projectId);

    List<ProjectTask> findByParentTaskId(UUID parentTaskId);

    List<ProjectTask> findByAssigneeId(UUID assigneeId);

    List<ProjectTask> findByAssigneeIdAndStatus(UUID assigneeId, TaskStatus status);

    List<ProjectTask> findByStatus(TaskStatus status);

    Page<ProjectTask> findByProjectId(UUID projectId, Pageable pageable);

    Page<ProjectTask> findByAssigneeId(UUID assigneeId, Pageable pageable);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.project.id = :projectId AND pt.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY pt.startDate ASC")
    List<ProjectTask> findOpenTasksByProject(@Param("projectId") UUID projectId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.assigneeId = :assigneeId AND pt.endDate < :date AND pt.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<ProjectTask> findOverdueTasksByAssignee(@Param("assigneeId") UUID assigneeId, @Param("date") LocalDate date);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.project.id = :projectId AND pt.isMilestone = true ORDER BY pt.endDate ASC")
    List<ProjectTask> findMilestonesByProject(@Param("projectId") UUID projectId);

    boolean existsByProjectIdAndCode(UUID projectId, String code);
}