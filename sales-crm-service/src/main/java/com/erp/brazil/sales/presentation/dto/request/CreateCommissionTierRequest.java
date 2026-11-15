package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCommissionTierRequest(
        @NotNull Integer tierOrder,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal rate,
        BigDecimal fixedAmount
) {}