package com.erp.brazil.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TrialBalanceResponse(
    UUID fiscalPeriodId,
    LocalDate asOfDate,
    List<AccountBalanceDTO> accounts,
    BigDecimal totalDebits,
    BigDecimal totalCredits,
    boolean balanced
) {
    public static TrialBalanceResponse of(UUID fiscalPeriodId, LocalDate asOfDate, List<AccountBalanceDTO> accounts) {
        BigDecimal totalDebits = accounts.stream()
                .map(AccountBalanceDTO::debitTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = accounts.stream()
                .map(AccountBalanceDTO::creditTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new TrialBalanceResponse(fiscalPeriodId, asOfDate, accounts, totalDebits, totalCredits,
                totalDebits.compareTo(totalCredits) == 0);
    }
}