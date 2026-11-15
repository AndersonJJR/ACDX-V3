package com.erp.brazil.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BalanceSheetResponse(
    UUID fiscalPeriodId,
    LocalDate asOfDate,
    List<BalanceSheetSectionDTO> assets,
    List<BalanceSheetSectionDTO> liabilities,
    List<BalanceSheetSectionDTO> equity,
    BigDecimal totalAssets,
    BigDecimal totalLiabilities,
    BigDecimal totalEquity,
    boolean balanced
) {
    public record BalanceSheetSectionDTO(
        String code,
        String name,
        AccountType accountType,
        BigDecimal amount,
        List<BalanceSheetSectionDTO> children
    ) {
        public enum AccountType {
            ASSET, LIABILITY, EQUITY
        }
    }
}