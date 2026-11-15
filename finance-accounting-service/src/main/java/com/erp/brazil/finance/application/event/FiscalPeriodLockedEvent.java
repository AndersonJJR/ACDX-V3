package com.erp.brazil.finance.application.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FiscalPeriodLockedEvent(
    UUID eventId,
    UUID fiscalPeriodId,
    Integer fiscalYear,
    Integer periodNumber,
    LocalDate lockDate,
    String lockedBy,
    LocalDateTime occurredAt
) {}