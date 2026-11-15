package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.domain.model.Project;
import com.erp.brazil.manufacturing.domain.model.ProjectTask;
import com.erp.brazil.manufacturing.domain.model.Timesheet;
import com.erp.brazil.manufacturing.domain.model.Project.ProjectStatus;
import com.erp.brazil.manufacturing.domain.model.Project.ProjectType;
import com.erp.brazil.manufacturing.domain.model.ProjectTask.TaskStatus;
import com.erp.brazil.manufacturing.domain.model.ProjectTask.Priority;
import com.erp.brazil.manufacturing.domain.repository.ProjectRepository;
import com.erp.brazil.manufacturing.domain.repository.ProjectTaskRepository;
import com.erp.brazil.manufacturing.domain.repository.TimesheetRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.ProjectDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Projects", description = "Project management")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final TimesheetRepository timesheetRepository;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectDto.ProjectResponse> createProject(@Valid @RequestBody ProjectDto.CreateProjectRequest request) {
        log.info("Creating project {}", request.getCode());

        Project project = Project.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .customerId(request.getCustomerId())
                .projectManagerId(request.getProjectManagerId())
                .projectType(ProjectType.valueOf(request.getProjectType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budgetHours(request.getBudgetHours() != null ? request.getBudgetHours() : BigDecimal.ZERO)
                .budgetCost(request.getBudgetCost() != null ? request.getBudgetCost() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "BRL")
                .billingType(request.getBillingType())
                .contractValue(request.getContractValue() != null ? request.getContractValue() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .status(ProjectStatus.PLANNING)
                .build();

        projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectDto.ProjectResponse.fromEntity(project));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectDto.ProjectResponse> getProject(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        return ResponseEntity.ok(ProjectDto.ProjectResponse.fromEntity(project));
    }

    @GetMapping
    @Operation(summary = "List projects with pagination")
    public ResponseEntity<Page<ProjectDto.ProjectResponse>> listProjects(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID projectManagerId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Project> projects;
        if (customerId != null) {
            projects = projectRepository.findByCustomerId(customerId, pageable);
        } else if (projectManagerId != null) {
            projects = projectRepository.findByProjectManagerId(projectManagerId, pageable);
        } else if (status != null) {
            projects = projectRepository.findByStatus(ProjectStatus.valueOf(status), pageable);
        } else {
            projects = projectRepository.findAll(pageable);
        }
        return ResponseEntity.ok(projects.map(ProjectDto.ProjectResponse::fromEntity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ProjectDto.ProjectResponse> updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectDto.CreateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCustomerId(request.getCustomerId());
        project.setProjectManagerId(request.getProjectManagerId());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBudgetHours(request.getBudgetHours());
        project.setBudgetCost(request.getBudgetCost());
        project.setContractValue(request.getContractValue());
        project.setNotes(request.getNotes());

        projectRepository.save(project);
        return ResponseEntity.ok(ProjectDto.ProjectResponse.fromEntity(project));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate project")
    public ResponseEntity<ProjectDto.ProjectResponse> activateProject(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        project.setStatus(ProjectStatus.ACTIVE);
        project.setActualStartDate(LocalDate.now());
        projectRepository.save(project);
        return ResponseEntity.ok(ProjectDto.ProjectResponse.fromEntity(project));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete project")
    public ResponseEntity<ProjectDto.ProjectResponse> completeProject(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        project.setStatus(ProjectStatus.COMPLETED);
        project.setActualEndDate(LocalDate.now());
        projectRepository.save(project);
        return ResponseEntity.ok(ProjectDto.ProjectResponse.fromEntity(project));
    }

    @PatchMapping("/{id}/hold")
    @Operation(summary = "Put project on hold")
    public ResponseEntity<ProjectDto.ProjectResponse> holdProject(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        project.setStatus(ProjectStatus.ON_HOLD);
        projectRepository.save(project);
        return ResponseEntity.ok(ProjectDto.ProjectResponse.fromEntity(project));
    }

    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get project tasks")
    public ResponseEntity<List<ProjectDto.ProjectTaskDto>> getProjectTasks(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        List<ProjectTask> tasks = projectTaskRepository.findByProjectIdAndParentTaskIdIsNull(id);
        return ResponseEntity.ok(tasks.stream().map(ProjectDto.ProjectTaskDto::fromEntity).toList());
    }

    @PostMapping("/{projectId}/tasks")
    @Operation(summary = "Create project task")
    public ResponseEntity<ProjectDto.ProjectTaskDto> createProjectTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectDto.CreateProjectTaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        ProjectTask task = ProjectTask.builder()
                .project(project)
                .parentTaskId(request.getParentTaskId())
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .priority(Priority.valueOf(request.getPriority() != null ? request.getPriority() : "NORMAL"))
                .assigneeId(request.getAssigneeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .estimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : BigDecimal.ZERO)
                .isMilestone(request.getIsMilestone() != null ? request.getIsMilestone() : false)
                .isBillable(request.getIsBillable() != null ? request.getIsBillable() : true)
                .billingRate(request.getBillingRate())
                .costRate(request.getCostRate())
                .currency(request.getCurrency() != null ? request.getCurrency() : "BRL")
                .notes(request.getNotes())
                .status(TaskStatus.NOT_STARTED)
                .build();

        if (request.getParentTaskId() != null) {
            ProjectTask parent = projectTaskRepository.findById(request.getParentTaskId()).orElseThrow();
            parent.addSubTask(task);
        } else {
            project.addTask(task);
        }

        projectTaskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectDto.ProjectTaskDto.fromEntity(task));
    }

    @GetMapping("/{id}/timesheets")
    @Operation(summary = "Get project timesheets")
    public ResponseEntity<List<ProjectDto.TimesheetDto>> getProjectTimesheets(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        List<Timesheet> timesheets = timesheetRepository.findByProjectId(id);
        return ResponseEntity.ok(timesheets.stream().map(ProjectDto.TimesheetDto::fromEntity).toList());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project (only if planning)")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        if (project.getStatus() != ProjectStatus.PLANNING) {
            throw new IllegalStateException("Can only delete projects in planning status");
        }
        projectRepository.delete(project);
        return ResponseEntity.noContent().build();
    }
}