package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateJournalEntryRequest(
    @NotBlank @Size(max = 50) String reference,
    @NotNull LocalDate entryDate,
    @NotNull UUID fiscalPeriodId,
    @Size(max = 500) String description,
    @NotNull @Valid List<CreateJournalEntryLineRequest> lines
) {
    public record CreateJournalEntryLineRequest(
        @NotNull UUID accountId,
        UUID costCenterId,
        @NotNull Integer lineNumber,
        @Size(max = 500) String description,
        @NotNull java.math.BigDecimal debitAmount,
        @NotNull java.math.BigDecimal creditAmount
    ) {
        public boolean isValid() {
            return (debitAmount.compareTo(java.math.BigDecimal.ZERO) > 0) ^ (creditAmount.compareTo(java.math.BigDecimal.ZERO) > 0);
        }
    }
}