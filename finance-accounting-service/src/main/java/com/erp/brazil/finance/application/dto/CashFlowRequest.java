package com.erp.brazil.finance.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CashFlowRequest(
    @NotNull UUID fiscalPeriodId,
    UUID companyId,
    LocalDate periodStart,
    LocalDate periodEnd,
    CashFlowMethod method
) {
    public enum CashFlowMethod {
        DIRECT, INDIRECT
    }
}