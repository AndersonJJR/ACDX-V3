package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.LeaveBalance;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveBalanceDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long employeeId,
    Long leaveTypeId,
    int referenceYear,
    BigDecimal accruedDays,
    BigDecimal usedDays,
    BigDecimal pendingDays,
    BigDecimal carriedOverDays,
    BigDecimal expiredDays,
    BigDecimal availableDays,
    LocalDate lastAccrualDate
) {
    public static LeaveBalanceDTO from(LeaveBalance balance) {
        return new LeaveBalanceDTO(
            balance.uuid(), balance.employeeId(), balance.leaveTypeId(), balance.referenceYear(),
            balance.accruedDays(), balance.usedDays(), balance.pendingDays(), balance.carriedOverDays(),
            balance.expiredDays(), balance.availableDays(), balance.lastAccrualDate()
        );
    }
}