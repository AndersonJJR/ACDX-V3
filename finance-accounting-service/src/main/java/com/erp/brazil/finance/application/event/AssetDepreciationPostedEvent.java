package com.erp.brazil.finance.application.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AssetDepreciationPostedEvent(
    UUID eventId,
    UUID journalEntryId,
    String reference,
    LocalDate entryDate,
    UUID fiscalPeriodId,
    BigDecimal totalAmount,
    List<AssetDepreciationLineEvent> lines,
    String postedBy,
    LocalDateTime postedAt,
    LocalDateTime occurredAt
) {
    public record AssetDepreciationLineEvent(
        UUID assetId,
        String assetCode,
        String assetName,
        UUID scheduleId,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal amount
    ) {}
}