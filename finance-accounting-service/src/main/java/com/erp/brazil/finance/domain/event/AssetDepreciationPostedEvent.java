package com.erp.brazil.finance.domain.event;

import com.erp.brazil.finance.domain.model.DepreciationEntry;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssetDepreciationPostedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID depreciationEntryId,
        UUID assetId,
        String assetCode,
        UUID fiscalPeriodId,
        LocalDate depreciationDate,
        java.math.BigDecimal depreciationAmount,
        java.math.BigDecimal accumulatedDepreciation,
        java.math.BigDecimal bookValue,
        UUID journalEntryId
) {
    public static AssetDepreciationPostedEvent from(DepreciationEntry depreciationEntry) {
        return new AssetDepreciationPostedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                depreciationEntry.getId(),
                depreciationEntry.getAsset().getId(),
                depreciationEntry.getAsset().getCode(),
                depreciationEntry.getFiscalPeriod().getId(),
                depreciationEntry.getDepreciationDate(),
                depreciationEntry.getDepreciationAmount(),
                depreciationEntry.getAccumulatedDepreciation(),
                depreciationEntry.getBookValue(),
                depreciationEntry.getJournalEntry() != null ? depreciationEntry.getJournalEntry().getId() : null
        );
    }
}