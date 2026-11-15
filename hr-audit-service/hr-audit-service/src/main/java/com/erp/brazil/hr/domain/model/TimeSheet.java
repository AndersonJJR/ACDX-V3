package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeSheet(
    Long id,
    UUID uuid,
    Long employeeId,
    int referenceMonth,
    int referenceYear,
    BigDecimal totalWorkedHours,
    BigDecimal totalOvertimeHours,
    BigDecimal totalNightHours,
    BigDecimal totalAbsenceHours,
    int totalLateMinutes,
    int totalEarlyDepartureMinutes,
    int daysWorked,
    int daysAbsent,
    int daysHoliday,
    int daysVacation,
    int daysMedicalLeave,
    TimeSheetStatus status,
    LocalDateTime calculatedAt,
    Long calculatedBy,
    LocalDateTime approvedAt,
    Long approvedBy,
    LocalDateTime closedAt,
    Long closedBy,
    String observations
) {
    public enum TimeSheetStatus {
        OPEN, CALCULATED, APPROVED, CLOSED, DISPUTED
    }

    public LocalDate periodStart() {
        return LocalDate.of(referenceYear, referenceMonth, 1);
    }

    public LocalDate periodEnd() {
        return periodStart().withDayOfMonth(periodStart().lengthOfMonth());
    }

    public boolean isClosed() {
        return status == TimeSheetStatus.CLOSED;
    }
}