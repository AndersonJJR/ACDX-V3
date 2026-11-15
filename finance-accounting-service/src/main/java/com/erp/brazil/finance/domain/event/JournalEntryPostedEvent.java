package com.erp.brazil.finance.domain.event;

import com.erp.brazil.finance.domain.model.JournalEntry;
import java.time.LocalDateTime;
import java.util.UUID;

public record JournalEntryPostedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID journalEntryId,
        String reference,
        LocalDateTime postedAt,
        String postedBy,
        String fiscalPeriodId,
        LocalDate entryDate,
        java.math.BigDecimal totalDebit,
        java.math.BigDecimal totalCredit
) {
    public static JournalEntryPostedEvent from(JournalEntry journalEntry) {
        return new JournalEntryPostedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                journalEntry.getId(),
                journalEntry.getReference(),
                journalEntry.getPostedAt(),
                journalEntry.getPostedBy(),
                journalEntry.getFiscalPeriod().getId().toString(),
                journalEntry.getEntryDate(),
                journalEntry.getTotalDebit(),
                journalEntry.getTotalCredit()
        );
    }
}