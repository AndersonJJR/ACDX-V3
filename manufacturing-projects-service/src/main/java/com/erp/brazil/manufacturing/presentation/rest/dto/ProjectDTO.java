package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProjectDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
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
        private List<ProjectTaskDTO.Response> tasks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskRequest {
        private UUID parentTaskId;
        private String code;
        private String name;
        private String description;
        private String priority;
        private UUID assigneeId;
        private LocalDate startDate;
        private LocalDate endDate.
        private BigDecimal estimatedHours.
        private Boolean isMilestone.
        private Boolean isBillable.
        private BigDecimal billingRate.
        private BigDecimal costRate.
        private String currency.
        private String notes.
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskResponse {
        private UUID id;
        private UUID projectId;
        private UUID parentTaskId;
        private String code.
        private String name.
        private String description.
        private String status.
        private String priority.
        private UUID assigneeId.
        private LocalDate startDate.
        private LocalDate endDate.
        private LocalDate actualStartDate.
        private LocalDate actualEndDate.
        private BigDecimal estimatedHours.
        private BigDecimal actualHours.
        private BigDecimal remainingHours.
        private BigDecimal progressPercentage.
        private Boolean isMilestone.
        private Boolean isBillable.
        private BigDecimal billingRate.
        private BigDecimal costRate.
        private String currency.
        private String notes.
        private List<TaskResponse> subTasks.
        private LocalDateTime createdAt.
        private LocalDateTime updatedAt.
    }
}