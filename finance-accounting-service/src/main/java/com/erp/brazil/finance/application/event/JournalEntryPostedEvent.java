package com.erp.brazil.finance.application.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JournalEntryPostedEvent(
    UUID eventId,
    UUID journalEntryId,
    String reference,
    LocalDate entryDate,
    UUID fiscalPeriodId,
    Integer fiscalYear,
    Integer periodNumber,
    BigDecimal totalDebit,
    BigDecimal totalCredit,
    List<JournalEntryLineEvent> lines,
    String postedBy,
    LocalDateTime postedAt,
    LocalDateTime occurredAt
) {
    public record JournalEntryLineEvent(
        UUID accountId,
        String accountCode,
        String accountName,
        UUID costCenterId,
        String costCenterCode,
        BigDecimal debitAmount,
        BigDecimal creditAmount
    ) {}
}