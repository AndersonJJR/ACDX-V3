package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.LeaveRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveRequestDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    @NotNull Long employeeId,
    @NotNull Long leaveTypeId,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
    @NotNull @DecimalMin("0.01") BigDecimal totalDays,
    BigDecimal totalHours,
    String reason,
    String attachmentUrl,
    LeaveRequest.LeaveStatus status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime requestedAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime approvedAt,
    Long approvedBy,
    String approvalNotes,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime rejectedAt,
    Long rejectedBy,
    String rejectionReason,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime cancelledAt,
    Long cancelledBy,
    String cancellationReason
) {
    public static LeaveRequestDTO from(LeaveRequest request) {
        return new LeaveRequestDTO(
            request.uuid(), request.employeeId(), request.leaveTypeId(), request.startDate(), request.endDate(),
            request.startTime(), request.endTime(), request.totalDays(), request.totalHours(),
            request.reason(), request.attachmentUrl(), request.status(), request.requestedAt(),
            request.approvedAt(), request.approvedBy(), request.approvalNotes(), request.rejectedAt(),
            request.rejectedBy(), request.rejectionReason(), request.cancelledAt(), request.cancelledBy(),
            request.cancellationReason()
        );
    }

    public long durationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}