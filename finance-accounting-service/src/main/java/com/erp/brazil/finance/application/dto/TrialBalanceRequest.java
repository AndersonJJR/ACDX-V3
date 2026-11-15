package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record TrialBalanceRequest(
    @NotNull UUID fiscalPeriodId,
    UUID companyId,
    LocalDate asOfDate,
    boolean includeZeroBalances
) {}