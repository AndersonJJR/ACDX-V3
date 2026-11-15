package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveRequest(
    Long id,
    UUID uuid,
    Long employeeId,
    Long leaveTypeId,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime startTime,
    LocalDateTime endTime,
    BigDecimal totalDays,
    BigDecimal totalHours,
    String reason,
    String attachmentUrl,
    LeaveStatus status,
    LocalDateTime requestedAt,
    LocalDateTime approvedAt,
    Long approvedBy,
    String approvalNotes,
    LocalDateTime rejectedAt,
    Long rejectedBy,
    String rejectionReason,
    LocalDateTime cancelledAt,
    Long cancelledBy,
    String cancellationReason
) {
    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED, EXPIRED
    }

    public boolean isPending() {
        return status == LeaveStatus.PENDING;
    }

    public boolean canApprove() {
        return status == LeaveStatus.PENDING;
    }

    public long durationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}