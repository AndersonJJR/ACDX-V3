package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.domain.model.Project;
import com.erp.brazil.manufacturing.domain.model.ProjectTask;
import com.erp.brazil.manufacturing.domain.repository.ProjectRepository;
import com.erp.brazil.manufacturing.domain.repository.ProjectTaskRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.ProjectDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;

    public ProjectController(ProjectRepository projectRepository, ProjectTaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @PostMapping
    public ResponseEntity<ProjectDTO.Response> create(@RequestBody ProjectDTO.Request request) {
        Project project = mapToEntity(request);
        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO.Response> getById(@PathVariable UUID id) {
        return projectRepository.findById(id)
            .map(this::mapToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProjectDTO.Response> getByCode(@PathVariable String code) {
        return projectRepository.findByCode(code)
            .map(this::mapToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<ProjectDTO.Response>> getAll(Pageable pageable) {
        Page<Project> page = projectRepository.findAll(pageable);
        return ResponseEntity.ok(page.map(this::mapToResponse));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<ProjectDTO.Response>> getByCustomerId(@PathVariable UUID customerId, Pageable pageable) {
        Page<Project> page = projectRepository.findByCustomerId(customerId, pageable);
        return ResponseEntity.ok(page.map(this::mapToResponse));
    }

    @GetMapping("/manager/{projectManagerId}")
    public ResponseEntity<Page<ProjectDTO.Response>> getByProjectManagerId(@PathVariable UUID projectManagerId, Pageable pageable) {
        Page<Project> page = projectRepository.findByProjectManagerId(projectManagerId, pageable);
        return ResponseEntity.ok(page.map(this::mapToResponse));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProjectDTO.Response>> getByStatus(@PathVariable Project.ProjectStatus status, Pageable pageable) {
        Page<Project> page = projectRepository.findByStatus(status, pageable);
        return ResponseEntity.ok(page.map(this::mapToResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO.Response> update(@PathVariable UUID id, @RequestBody ProjectDTO.Request request) {
        return projectRepository.findById(id)
            .map(existing -> {
                updateEntity(existing, request);
                Project saved = projectRepository.save(existing);
                return ResponseEntity.ok(mapToResponse(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Task endpoints
    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<ProjectDTO.TaskResponse> createTask(@PathVariable UUID projectId, @RequestBody ProjectDTO.TaskRequest request) {
        return projectRepository.findById(projectId)
            .map(project -> {
                ProjectTask task = mapToTaskEntity(request);
                task.setProject(project);
                if (request.getParentTaskId() != null) {
                    taskRepository.findById(request.getParentTaskId())
                        .ifPresent(parent -> task.setParentTaskId(parent.getId()));
                }
                ProjectTask saved = taskRepository.save(task);
                project.addTask(saved);
                projectRepository.save(project);
                return ResponseEntity.ok(mapTaskToResponse(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<ProjectDTO.TaskResponse>> getTasks(@PathVariable UUID projectId) {
        return projectRepository.findById(projectId)
            .map(project -> ResponseEntity.ok(
                project.getTasks().stream().map(this::mapTaskToResponse).collect(Collectors.toList())
            ))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ProjectDTO.TaskResponse> getTaskById(@PathVariable UUID taskId) {
        return taskRepository.findById(taskId)
            .map(this::mapTaskToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ProjectDTO.TaskResponse> updateTask(@PathVariable UUID taskId, @RequestBody ProjectDTO.TaskRequest request) {
        return taskRepository.findById(taskId)
            .map(existing -> {
                updateTaskEntity(existing, request);
                ProjectTask saved = taskRepository.save(existing);
                return ResponseEntity.ok(mapTaskToResponse(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Project mapToEntity(ProjectDTO.Request request) {
        return Project.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .customerId(request.getCustomerId())
            .projectManagerId(request.getProjectManagerId())
            .status(Project.ProjectStatus.PLANNING)
            .projectType(Project.ProjectType.valueOf(request.getProjectType()))
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .budgetHours(request.getBudgetHours() != null ? request.getBudgetHours() : BigDecimal.ZERO)
            .budgetCost(request.getBudgetCost() != null ? request.getBudgetCost() : BigDecimal.ZERO)
            .currency(request.getCurrency() != null ? request.getCurrency() : "BRL")
            .billingType(request.getBillingType())
            .contractValue(request.getContractValue() != null ? request.getContractValue() : BigDecimal.ZERO)
            .notes(request.getNotes())
            .build();
    }

    private void updateEntity(Project project, ProjectDTO.Request request) {
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setProjectManagerId(request.getProjectManagerId());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setBudgetHours(request.getBudgetHours());
        project.setBudgetCost(request.getBudgetCost());
        project.setBillingType(request.getBillingType());
        project.setContractValue(request.getContractValue());
        project.setNotes(request.getNotes());
    }

    private ProjectDTO.Response mapToResponse(Project project) {
        return ProjectDTO.Response.builder()
            .id(project.getId())
            .code(project.getCode())
            .name(project.getName())
            .description(project.getDescription())
            .customerId(project.getCustomerId())
            .projectManagerId(project.getProjectManagerId())
            .status(project.getStatus().name())
            .projectType(project.getProjectType().name())
            .startDate(project.getStartDate())
            .endDate(project.getEndDate())
            .actualStartDate(project.getActualStartDate())
            .actualEndDate(project.getActualEndDate())
            .budgetHours(project.getBudgetHours())
            .actualHours(project.getActualHours())
            .budgetCost(project.getBudgetCost())
            .actualCost(project.getActualCost())
            .currency(project.getCurrency())
            .billingType(project.getBillingType())
            .contractValue(project.getContractValue())
            .invoicedAmount(project.getInvoicedAmount())
            .notes(project.getNotes())
            .tasks(project.getTasks().stream().map(this::mapTaskToResponse).collect(Collectors.toList()))
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
    }

    private ProjectTask mapToTaskEntity(ProjectDTO.TaskRequest request) {
        return ProjectTask.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .status(ProjectTask.TaskStatus.NOT_STARTED)
            .priority(ProjectTask.Priority.valueOf(request.getPriority()))
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
            .build();
    }

    private void updateTaskEntity(ProjectTask task, ProjectDTO.TaskRequest request) {
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setPriority(ProjectTask.Priority.valueOf(request.getPriority()));
        task.setAssigneeId(request.getAssigneeId());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setIsMilestone(request.getIsMilestone());
        task.setIsBillable(request.getIsBillable());
        task.setBillingRate(request.getBillingRate());
        task.setCostRate(request.getCostRate());
        task.setNotes(request.getNotes());
    }

    private ProjectDTO.TaskResponse mapTaskToResponse(ProjectTask task) {
        return ProjectDTO.TaskResponse.builder()
            .id(task.getId())
            .projectId(task.getProject().getId())
            .parentTaskId(task.getParentTaskId())
            .code(task.getCode())
            .name(task.getName())
            .description(task.getDescription())
            .status(task.getStatus().name())
            .priority(task.getPriority().name())
            .assigneeId(task.getAssigneeId())
            .startDate(task.getStartDate())
            .endDate(task.getEndDate())
            .actualStartDate(task.getActualStartDate())
            .actualEndDate(task.getActualEndDate())
            .estimatedHours(task.getEstimatedHours())
            .actualHours(task.getActualHours())
            .remainingHours(task.getRemainingHours())
            .progressPercentage(task.getProgressPercentage())
            .isMilestone(task.getIsMilestone())
            .isBillable(task.getIsBillable())
            .billingRate(task.getBillingRate())
            .costRate(task.getCostRate())
            .currency(task.getCurrency())
            .notes(task.getNotes())
            .subTasks(task.getSubTasks().stream().map(this::mapTaskToResponse).collect(Collectors.toList()))
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}