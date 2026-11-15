package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.application.usecase.LogTimesheetUseCase;
import com.erp.brazil.manufacturing.domain.model.Timesheet;
import com.erp.brazil.manufacturing.domain.model.Timesheet.TimesheetStatus;
import com.erp.brazil.manufacturing.domain.repository.TimesheetRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.ProjectDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Timesheets", description = "Timesheet management")
public class TimesheetController {

    private final TimesheetRepository timesheetRepository;
    private final LogTimesheetUseCase logTimesheetUseCase;

    @PostMapping("/log")
    @Operation(summary = "Log timesheet entry")
    public ResponseEntity<ProjectDto.TimesheetDto> logTimesheet(@Valid @RequestBody ProjectDto.LogTimesheetRequest request) {
        log.info("Logging timesheet for employee {} project {}", request.getEmployeeId(), request.getProjectId());

        var command = LogTimesheetUseCase.LogTimesheetCommand.builder()
                .projectId(request.getProjectId())
                .taskId(request.getTaskId())
                .employeeId(request.getEmployeeId())
                .workDate(request.getWorkDate())
                .hoursWorked(request.getHoursWorked())
                .overtimeHours(request.getOvertimeHours())
                .description(request.getDescription())
                .isBillable(request.getIsBillable() != null ? request.getIsBillable() : true)
                .billingRate(request.getBillingRate())
                .costRate(request.getCostRate())
                .currency(request.getCurrency() != null ? request.getCurrency() : "BRL")
                .createdBy(request.getCreatedBy())
                .build();

        var result = logTimesheetUseCase.execute(command);
        Timesheet timesheet = timesheetRepository.findById(result.timesheetId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectDto.TimesheetDto.fromEntity(timesheet));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get timesheet by ID")
    public ResponseEntity<ProjectDto.TimesheetDto> getTimesheet(@PathVariable UUID id) {
        Timesheet timesheet = timesheetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found: " + id));
        return ResponseEntity.ok(ProjectDto.TimesheetDto.fromEntity(timesheet));
    }

    @GetMapping
    @Operation(summary = "List timesheets with pagination")
    public ResponseEntity<Page<ProjectDto.TimesheetDto>> listTimesheets(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Timesheet> timesheets;
        if (employeeId != null && startDate != null && endDate != null) {
            timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate, pageable);
        } else if (employeeId != null) {
            timesheets = timesheetRepository.findByEmployeeId(employeeId, pageable);
        } else if (projectId != null) {
            timesheets = timesheetRepository.findByProjectId(projectId, pageable);
        } else if (taskId != null) {
            timesheets = timesheetRepository.findByTaskId(taskId, pageable);
        } else if (status != null) {
            timesheets = timesheetRepository.findByStatus(TimesheetStatus.valueOf(status), pageable);
        } else {
            timesheets = timesheetRepository.findAll(pageable);
        }
        return ResponseEntity.ok(timesheets.map(ProjectDto.TimesheetDto::fromEntity));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get timesheets by employee")
    public ResponseEntity<List<ProjectDto.TimesheetDto>> getTimesheetsByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            List<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);
            return ResponseEntity.ok(timesheets.stream().map(ProjectDto.TimesheetDto::fromEntity).toList());
        }
        List<Timesheet> timesheets = timesheetRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(timesheets.stream().map(ProjectDto.TimesheetDto::fromEntity).toList());
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get timesheets by project")
    public ResponseEntity<List<ProjectDto.TimesheetDto>> getTimesheetsByProject(@PathVariable UUID projectId) {
        List<Timesheet> timesheets = timesheetRepository.findByProjectId(projectId);
        return ResponseEntity.ok(timesheets.stream().map(ProjectDto.TimesheetDto::fromEntity).toList());
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit timesheet for approval")
    public ResponseEntity<ProjectDto.TimesheetDto> submitTimesheet(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDto.SubmitTimesheetRequest request) {
        logTimesheetUseCase.submitTimesheet(id, request.getSubmittedBy());
        Timesheet timesheet = timesheetRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(ProjectDto.TimesheetDto.fromEntity(timesheet));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve timesheet")
    public ResponseEntity<ProjectDto.TimesheetDto> approveTimesheet(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDto.ApproveTimesheetRequest request) {
        logTimesheetUseCase.approveTimesheet(id, request.getApprovedBy());
        Timesheet timesheet = timesheetRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(ProjectDto.TimesheetDto.fromEntity(timesheet));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject timesheet")
    public ResponseEntity<ProjectDto.TimesheetDto> rejectTimesheet(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectDto.RejectTimesheetRequest request) {
        logTimesheetUseCase.rejectTimesheet(id, request.getRejectedBy(), request.getReason());
        Timesheet timesheet = timesheetRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(ProjectDto.TimesheetDto.fromEntity(timesheet));
    }

    @GetMapping("/project/{projectId}/hours-summary")
    @Operation(summary = "Get approved hours summary for project")
    public ResponseEntity<ProjectHoursSummary> getProjectHoursSummary(@PathVariable UUID projectId) {
        BigDecimal totalHours = timesheetRepository.sumApprovedHoursByProject(projectId);
        return ResponseEntity.ok(new ProjectHoursSummary(projectId, totalHours != null ? totalHours : BigDecimal.ZERO));
    }

    @GetMapping("/task/{taskId}/hours-summary")
    @Operation(summary = "Get approved hours summary for task")
    public ResponseEntity<TaskHoursSummary> getTaskHoursSummary(@PathVariable UUID taskId) {
        BigDecimal totalHours = timesheetRepository.sumApprovedHoursByTask(taskId);
        return ResponseEntity.ok(new TaskHoursSummary(taskId, totalHours != null ? totalHours : BigDecimal.ZERO));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectHoursSummary {
        private UUID projectId;
        private BigDecimal totalApprovedHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskHoursSummary {
        private UUID taskId;
        private BigDecimal totalApprovedHours;
    }
}