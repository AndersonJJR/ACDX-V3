package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record JournalEntryLineDTO(
    UUID id,
    @NotNull UUID accountId,
    UUID costCenterId,
    @NotNull Integer lineNumber,
    @Size(max = 500) String description,
    @NotNull BigDecimal debitAmount,
    @NotNull BigDecimal creditAmount
) {
    public boolean isDebit() {
        return debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCredit() {
        return creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}