package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record JournalEntryDTO(
    UUID id,
    @NotBlank @Size(max = 50) String reference,
    @NotNull LocalDate entryDate,
    @NotNull UUID fiscalPeriodId,
    @Size(max = 500) String description,
    JournalEntryStatus status,
    BigDecimal totalDebit,
    BigDecimal totalCredit,
    boolean reversed,
    UUID reversedEntryId,
    String reversalReason,
    List<JournalEntryLineDTO> lines
) {
    public boolean isBalanced() {
        return totalDebit.compareTo(totalCredit) == 0;
    }
}