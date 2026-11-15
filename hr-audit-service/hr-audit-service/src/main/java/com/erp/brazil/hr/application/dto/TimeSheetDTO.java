package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.TimeSheet;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TimeSheetDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
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
    TimeSheet.TimeSheetStatus status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime calculatedAt,
    Long calculatedBy,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime approvedAt,
    Long approvedBy,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime closedAt,
    Long closedBy,
    String observations
) {
    public static TimeSheetDTO from(TimeSheet sheet) {
        return new TimeSheetDTO(
            sheet.uuid(), sheet.employeeId(), sheet.referenceMonth(), sheet.referenceYear(),
            sheet.totalWorkedHours(), sheet.totalOvertimeHours(), sheet.totalNightHours(), sheet.totalAbsenceHours(),
            sheet.totalLateMinutes(), sheet.totalEarlyDepartureMinutes(), sheet.daysWorked(), sheet.daysAbsent(),
            sheet.daysHoliday(), sheet.daysVacation(), sheet.daysMedicalLeave(), sheet.status(),
            sheet.calculatedAt(), sheet.calculatedBy(), sheet.approvedAt(), sheet.approvedBy(),
            sheet.closedAt(), sheet.closedBy(), sheet.observations()
        );
    }

    public LocalDate periodStart() {
        return LocalDate.of(referenceYear, referenceMonth, 1);
    }

    public LocalDate periodEnd() {
        return periodStart().withDayOfMonth(periodStart().lengthOfMonth());
    }
}