package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "leave_request",
    indexes = {
        @Index(name = "idx_leave_request_employee", columnList = "employee_id"),
        @Index(name = "idx_leave_request_dates", columnList = "start_date,end_date"),
        @Index(name = "idx_leave_request_status", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @NotNull
    @Column(name = "total_days", nullable = false, precision = 4, scale = 2)
    private BigDecimal totalDays;

    @Column(name = "total_hours", precision = 5, scale = 2)
    private BigDecimal totalHours;

    @Size(max = 1000)
    @Column(name = "reason", length = 1000)
    private String reason;

    @Size(max = 500)
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private java.time.Instant requestedAt = java.time.Instant.now();

    @Column(name = "approved_at")
    private java.time.Instant approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Size(max = 500)
    @Column(name = "approval_notes", length = 500)
    private String approvalNotes;

    @Column(name = "rejected_at")
    private java.time.Instant rejectedAt;

    @Column(name = "rejected_by")
    private Long rejectedBy;

    @Size(max = 500)
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "cancelled_at")
    private java.time.Instant cancelledAt;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public boolean isPending() {
        return status == LeaveRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return status == LeaveRequestStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == LeaveRequestStatus.REJECTED;
    }

    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }

    public boolean isFinal() {
        return status.isFinal();
    }
}