package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveBalance(
    Long id,
    UUID uuid,
    Long employeeId,
    Long leaveTypeId,
    int referenceYear,
    BigDecimal accruedDays,
    BigDecimal usedDays,
    BigDecimal pendingDays,
    BigDecimal carriedOverDays,
    BigDecimal expiredDays,
    LocalDate lastAccrualDate
) {
    public BigDecimal availableDays() {
        return accruedDays.add(carriedOverDays).subtract(usedDays).subtract(pendingDays).subtract(expiredDays);
    }

    public boolean hasAvailableDays(BigDecimal requestedDays) {
        return availableDays().compareTo(requestedDays) >= 0;
    }

    public void accrueDays(BigDecimal days) {
        // This would create a new record with updated values
    }

    public void useDays(BigDecimal days) {
        // This would create a new record with updated values
    }
}