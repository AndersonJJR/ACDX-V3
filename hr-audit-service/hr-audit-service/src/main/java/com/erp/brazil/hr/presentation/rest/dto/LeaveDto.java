package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.LeaveCategory;
import com.erp.brazil.hr.domain.model.LeaveRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class LeaveDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveTypeRequest {
        private String code;
        private String name;
        private String description;
        private LeaveCategory category;
        private Integer maxDaysPerYear;
        private Boolean requiresApproval;
        private Boolean requiresDocument;
        private Boolean paid;
        private Boolean affectsBenefits;
        private String color;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveTypeResponse {
        private Long id;
        private String uuid;
        private String code;
        private String name;
        private String description;
        private LeaveCategory category;
        private String categoryDescription;
        private Integer maxDaysPerYear;
        private Boolean requiresApproval;
        private Boolean requiresDocument;
        private Boolean paid;
        private Boolean affectsBenefits;
        private String color;
        private Boolean isActive;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveRequestRequest {
        private Long leaveTypeId;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
        private String attachmentUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveRequestResponse {
        private Long id;
        private String uuid;
        private Long employeeId;
        private String employeeName;
        private Long leaveTypeId;
        private String leaveTypeCode;
        private String leaveTypeName;
        private LeaveCategory leaveCategory;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private BigDecimal totalDays;
        private BigDecimal totalHours;
        private String reason;
        private String attachmentUrl;
        private LeaveRequestStatus status;
        private String statusDescription;
        private java.time.Instant requestedAt;
        private java.time.Instant approvedAt;
        private Long approvedBy;
        private String approvedByName;
        private String approvalNotes;
        private java.time.Instant rejectedAt;
        private Long rejectedBy;
        private String rejectedByName;
        private String rejectionReason;
        private java.time.Instant cancelledAt;
        private Long cancelledBy;
        private String cancelledByName;
        private String cancellationReason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalRequest {
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectionRequest {
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancellationRequest {
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveBalanceResponse {
        private Long id;
        private String uuid;
        private Long employeeId;
        private String employeeName;
        private Long leaveTypeId;
        private String leaveTypeCode;
        private String leaveTypeName;
        private Integer referenceYear;
        private BigDecimal accruedDays;
        private BigDecimal usedDays;
        private BigDecimal pendingDays;
        private BigDecimal carriedOverDays;
        private BigDecimal expiredDays;
        private BigDecimal availableDays;
        private LocalDate lastAccrualDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<LeaveRequestResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}