package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.event.TimesheetSubmittedEvent;
import com.erp.brazil.manufacturing.domain.model.Timesheet;
import com.erp.brazil.manufacturing.domain.repository.ProjectRepository;
import com.erp.brazil.manufacturing.domain.repository.ProjectTaskRepository;
import com.erp.brazil.manufacturing.domain.repository.TimesheetRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class LogTimesheetUseCase {

    private final TimesheetRepository timesheetRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LogTimesheetUseCase(TimesheetRepository timesheetRepository,
                                ProjectRepository projectRepository,
                                ProjectTaskRepository taskRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.timesheetRepository = timesheetRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    public Timesheet logTime(UUID projectId, UUID taskId, UUID employeeId,
                              LocalDate workDate, BigDecimal hoursWorked,
                              BigDecimal overtimeHours, String description,
                              Boolean isBillable, BigDecimal billingRate,
                              BigDecimal costRate, String createdBy) {
        // Validate project exists
        projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        // Validate task if provided
        if (taskId != null) {
            taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
        }

        Timesheet timesheet = Timesheet.builder()
            .project(projectRepository.findById(projectId).get())
            .task(taskId != null ? taskRepository.findById(taskId).get() : null)
            .employeeId(employeeId)
            .workDate(workDate)
            .hoursWorked(hoursWorked)
            .overtimeHours(overtimeHours != null ? overtimeHours : BigDecimal.ZERO)
            .description(description)
            .isBillable(isBillable != null ? isBillable : true)
            .billingRate(billingRate)
            .costRate(costRate)
            .currency("BRL")
            .status(Timesheet.TimesheetStatus.DRAFT)
            .createdBy(createdBy)
            .build();

        return timesheetRepository.save(timesheet);
    }

    public Timesheet submitTimesheet(UUID timesheetId, String submittedBy) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + timesheetId));

        if (timesheet.getStatus() != Timesheet.TimesheetStatus.DRAFT) {
            throw new IllegalStateException("Timesheet cannot be submitted. Current status: " + timesheet.getStatus());
        }

        timesheet.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        timesheet.setSubmittedAt(LocalDateTime.now());

        Timesheet saved = timesheetRepository.save(timesheet);

        // Publish event
        eventPublisher.publishEvent(TimesheetSubmittedEvent.builder()
            .timesheetId(saved.getId())
            .projectId(saved.getProject().getId())
            .taskId(saved.getTask() != null ? saved.getTask().getId() : null)
            .employeeId(saved.getEmployeeId())
            .workDate(saved.getWorkDate())
            .hoursWorked(saved.getHoursWorked())
            .isBillable(saved.getIsBillable())
            .billingRate(saved.getBillingRate())
            .submittedBy(submittedBy)
            .build());

        return saved;
    }

    public Timesheet approveTimesheet(UUID timesheetId, String approvedBy) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + timesheetId));

        if (timesheet.getStatus() != Timesheet.TimesheetStatus.SUBMITTED) {
            throw new IllegalStateException("Timesheet cannot be approved. Current status: " + timesheet.getStatus());
        }

        timesheet.setStatus(Timesheet.TimesheetStatus.APPROVED);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheet.setApprovedBy(approvedBy);

        // Update project actual hours
        updateProjectHours(timesheet);

        return timesheetRepository.save(timesheet);
    }

    public Timesheet rejectTimesheet(UUID timesheetId, String rejectedBy, String reason) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + timesheetId));

        if (timesheet.getStatus() != Timesheet.TimesheetStatus.SUBMITTED) {
            throw new IllegalStateException("Timesheet cannot be rejected. Current status: " + timesheet.getStatus());
        }

        timesheet.setStatus(Timesheet.TimesheetStatus.REJECTED);
        timesheet.setRejectedAt(LocalDateTime.now());
        timesheet.setRejectedBy(rejectedBy);
        timesheet.setRejectionReason(reason);

        return timesheetRepository.save(timesheet);
    }

    public void approveBatch(java.util.List<UUID> timesheetIds, String approvedBy) {
        for (UUID id : timesheetIds) {
            try {
                approveTimesheet(id, approvedBy);
            } catch (Exception e) {
                System.err.println("Failed to approve timesheet " + id + ": " + e.getMessage());
            }
        }
    }

    private void updateProjectHours(Timesheet timesheet) {
        var project = timesheet.getProject();
        project.setActualHours(project.getActualHours().add(timesheet.getHoursWorked()));
        if (timesheet.getTask() != null) {
            var task = timesheet.getTask();
            task.setActualHours(task.getActualHours().add(timesheet.getHoursWorked()));
        }
        projectRepository.save(project);
    }
}