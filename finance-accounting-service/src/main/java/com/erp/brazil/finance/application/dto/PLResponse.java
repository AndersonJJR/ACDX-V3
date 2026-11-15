package com.erp.brazil.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PLResponse(
    UUID fiscalPeriodId,
    LocalDate periodStart,
    LocalDate periodEnd,
    List<PLSectionDTO> revenue,
    List<PLSectionDTO> expenses,
    List<PLSectionDTO> costs,
    BigDecimal totalRevenue,
    BigDecimal totalExpenses,
    BigDecimal totalCosts,
    BigDecimal grossProfit,
    BigDecimal operatingProfit,
    BigDecimal netProfit
) {
    public record PLSectionDTO(
        String code,
        String name,
        SectionType sectionType,
        BigDecimal amount,
        List<PLSectionDTO> children
    ) {
        public enum SectionType {
            REVENUE, EXPENSE, COST
        }
    }
}