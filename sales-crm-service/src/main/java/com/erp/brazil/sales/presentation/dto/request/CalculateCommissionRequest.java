package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CalculateCommissionRequest(
        @NotNull UUID companyId,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        @NotNull UUID calculatedById
) {}