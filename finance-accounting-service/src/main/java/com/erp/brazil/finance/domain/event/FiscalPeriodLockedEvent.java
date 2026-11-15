package com.erp.brazil.finance.domain.event;

import com.erp.brazil.finance.domain.model.FiscalPeriod;
import java.time.LocalDateTime;
import java.util.UUID;

public record FiscalPeriodLockedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID fiscalPeriodId,
        Integer fiscalYear,
        Integer periodNumber,
        LocalDate lockedAt,
        String lockedBy,
        String fiscalYearId
) {
    public static FiscalPeriodLockedEvent from(FiscalPeriod fiscalPeriod) {
        return new FiscalPeriodLockedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                fiscalPeriod.getId(),
                fiscalPeriod.getFiscalYear().getYear(),
                fiscalPeriod.getPeriodNumber(),
                fiscalPeriod.getLockedAt(),
                fiscalPeriod.getLockedBy(),
                fiscalPeriod.getFiscalYear().getId().toString()
        );
    }
}