package com.erp.brazil.finance.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceDTO(
    UUID accountId,
    String code,
    String name,
    BigDecimal debitTotal,
    BigDecimal creditTotal
) {
    public BigDecimal getBalance() {
        return debitTotal.subtract(creditTotal);
    }

    public boolean isDebitBalance() {
        return getBalance().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCreditBalance() {
        return getBalance().compareTo(BigDecimal.ZERO) < 0;
    }
}