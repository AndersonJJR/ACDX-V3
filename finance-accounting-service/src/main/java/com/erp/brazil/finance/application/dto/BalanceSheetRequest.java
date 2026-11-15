package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record BalanceSheetRequest(
    @NotNull UUID fiscalPeriodId,
    UUID companyId,
    LocalDate asOfDate,
    boolean comparative
) {}