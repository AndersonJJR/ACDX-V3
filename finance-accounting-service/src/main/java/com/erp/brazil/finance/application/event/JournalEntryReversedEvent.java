package com.erp.brazil.finance.application.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JournalEntryReversedEvent(
    UUID eventId,
    UUID originalEntryId,
    UUID reversalEntryId,
    String reference,
    LocalDate reversalDate,
    String reason,
    String reversedBy,
    LocalDateTime occurredAt
) {}