package com.erp.brazil.sales.presentation.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditStatusResponse(
        UUID customerId,
        String customerName,
        BigDecimal creditLimit,
        BigDecimal usedCredit,
        BigDecimal availableCredit,
        BigDecimal overdueAmount,
        Boolean isBlocked,
        String blockReason,
        Integer overdueInvoicesCount
) {}