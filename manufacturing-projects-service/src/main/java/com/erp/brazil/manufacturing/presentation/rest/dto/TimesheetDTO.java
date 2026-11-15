package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TimesheetDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
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
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
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
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubmitRequest {
        private String submittedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproveRequest {
        private String approvedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectRequest {
        private String rejectedBy;
        private String reason;
    }
}