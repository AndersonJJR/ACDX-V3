package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record PLRequest(
    @NotNull UUID fiscalPeriodId,
    UUID companyId,
    LocalDate periodStart,
    LocalDate periodEnd,
    boolean comparative
) {}