package com.erp.brazil.manufacturing.presentation.rest.dto;

import com.erp.brazil.manufacturing.domain.model.Project;
import com.erp.brazil.manufacturing.domain.model.ProjectTask;
import com.erp.brazil.manufacturing.domain.model.Timesheet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectResponse {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private UUID customerId;
        private UUID projectManagerId;
        private String status;
        private String projectType;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        private BigDecimal budgetHours;
        private BigDecimal actualHours;
        private BigDecimal budgetCost;
        private BigDecimal actualCost;
        private String currency;
        private String billingType;
        private BigDecimal contractValue;
        private BigDecimal invoicedAmount;
        private String notes;
        private List<ProjectTaskDto> tasks;
        private List<TimesheetDto> timesheets;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public static ProjectResponse fromEntity(Project project) {
            return ProjectResponse.builder()
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
                    .tasks(project.getTasks().stream().map(ProjectTaskDto::fromEntity).toList())
                    .timesheets(project.getTimesheets().stream().map(TimesheetDto::fromEntity).toList())
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .createdBy(project.getCreatedBy())
                    .updatedBy(project.getUpdatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectTaskDto {
        private UUID id;
        private UUID projectId;
        private UUID parentTaskId;
        private String code;
        private String name;
        private String description;
        private String status;
        private String priority;
        private UUID assigneeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        private BigDecimal estimatedHours;
        private BigDecimal actualHours;
        private BigDecimal remainingHours;
        private BigDecimal progressPercentage;
        private Boolean isMilestone;
        private Boolean isBillable;
        private BigDecimal billingRate;
        private BigDecimal costRate;
        private String currency;
        private String notes;
        private List<ProjectTaskDto> subTasks;
        private List<TimesheetDto> timesheets;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public static ProjectTaskDto fromEntity(ProjectTask task) {
            return ProjectTaskDto.builder()
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
                    .subTasks(task.getSubTasks().stream().map(ProjectTaskDto::fromEntity).toList())
                    .timesheets(task.getTimesheets().stream().map(TimesheetDto::fromEntity).toList())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    .createdBy(task.getCreatedBy())
                    .updatedBy(task.getUpdatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimesheetDto {
        private UUID id;
        private UUID projectId;
        private UUID taskId;
        private UUID employeeId;
        private LocalDate workDate;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal hoursWorked;
        private BigDecimal overtimeHours;
        private String description;
        private Boolean isBillable;
        private BigDecimal billingRate;
        private BigDecimal costRate;
        private String currency;
        private String status;
        private LocalDateTime submittedAt;
        private LocalDateTime approvedAt;
        private String approvedBy;
        private LocalDateTime rejectedAt;
        private String rejectedBy;
        private String rejectionReason;
        private UUID invoiceId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;

        public static TimesheetDto fromEntity(Timesheet ts) {
            return TimesheetDto.builder()
                    .id(ts.getId())
                    .projectId(ts.getProject().getId())
                    .taskId(ts.getTask() != null ? ts.getTask().getId() : null)
                    .employeeId(ts.getEmployeeId())
                    .workDate(ts.getWorkDate())
                    .periodStart(ts.getPeriodStart())
                    .periodEnd(ts.getPeriodEnd())
                    .hoursWorked(ts.getHoursWorked())
                    .overtimeHours(ts.getOvertimeHours())
                    .description(ts.getDescription())
                    .isBillable(ts.getIsBillable())
                    .billingRate(ts.getBillingRate())
                    .costRate(ts.getCostRate())
                    .currency(ts.getCurrency())
                    .status(ts.getStatus().name())
                    .submittedAt(ts.getSubmittedAt())
                    .approvedAt(ts.getApprovedAt())
                    .approvedBy(ts.getApprovedBy())
                    .rejectedAt(ts.getRejectedAt())
                    .rejectedBy(ts.getRejectedBy())
                    .rejectionReason(ts.getRejectionReason())
                    .invoiceId(ts.getInvoiceId())
                    .createdAt(ts.getCreatedAt())
                    .updatedAt(ts.getUpdatedAt())
                    .createdBy(ts.getCreatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProjectRequest {
        private String code;
        private String name;
        private String description;
        private UUID customerId;
        private UUID projectManagerId;
        private String projectType;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal budgetHours;
        private BigDecimal budgetCost;
        private String currency;
        private String billingType;
        private BigDecimal contractValue;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProjectTaskRequest {
        private UUID projectId;
        private UUID parentTaskId;
        private String code;
        private String name;
        private String description;
        private String priority;
        private UUID assigneeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal estimatedHours;
        private Boolean isMilestone;
        private Boolean isBillable;
        private BigDecimal billingRate;
        private BigDecimal costRate;
        private String currency;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogTimesheetRequest {
        private UUID projectId;
        private UUID taskId;
        private UUID employeeId;
        private LocalDate workDate;
        private BigDecimal hoursWorked;
        private BigDecimal overtimeHours;
        private String description;
        private Boolean isBillable;
        private BigDecimal billingRate;
        private BigDecimal costRate;
        private String currency;
        private String createdBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitTimesheetRequest {
        private String submittedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveTimesheetRequest {
        private String approvedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectTimesheetRequest {
        private String rejectedBy;
        private String reason;
    }
}