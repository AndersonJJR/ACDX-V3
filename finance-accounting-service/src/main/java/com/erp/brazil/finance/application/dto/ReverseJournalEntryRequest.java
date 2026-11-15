package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record ReverseJournalEntryRequest(
    @NotNull LocalDate reversalDate,
    @NotBlank @Size(max = 500) String reason,
    @NotBlank @Size(max = 200) String reversedBy
) {}