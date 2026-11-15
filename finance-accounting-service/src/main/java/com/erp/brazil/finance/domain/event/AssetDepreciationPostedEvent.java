package com.erp.brazil.finance.domain.event;

import com.erp.brazil.finance.domain.model.AssetDepreciationSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssetDepreciationPostedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID scheduleId,
        UUID assetId,
        String assetCode,
        LocalDate periodStart,
        LocalDate periodEnd,
        java.math.BigDecimal depreciationAmount,
        java.math.BigDecimal accumulatedDepreciation,
        java.math.BigDecimal bookValue,
        UUID journalEntryId
) {
    public static AssetDepreciationPostedEvent from(AssetDepreciationSchedule schedule) {
        return new AssetDepreciationPostedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                schedule.getId(),
                schedule.getAsset().getId(),
                schedule.getAsset().getCode(),
                schedule.getPeriodStart(),
                schedule.getPeriodEnd(),
                schedule.getDepreciationAmount(),
                schedule.getAccumulatedDepreciation(),
                schedule.getBookValue(),
                null // journalEntryId would need to be added to schedule
        );
    }
}