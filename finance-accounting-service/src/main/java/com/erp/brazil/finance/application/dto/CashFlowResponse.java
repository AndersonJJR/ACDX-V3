package com.erp.brazil.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CashFlowResponse(
    UUID fiscalPeriodId,
    LocalDate periodStart,
    LocalDate periodEnd,
    CashFlowMethod method,
    List<CashFlowSectionDTO> operatingActivities,
    List<CashFlowSectionDTO> investingActivities,
    List<CashFlowSectionDTO> financingActivities,
    BigDecimal netOperatingCashFlow,
    BigDecimal netInvestingCashFlow,
    BigDecimal netFinancingCashFlow,
    BigDecimal netCashFlow,
    BigDecimal beginningCash,
    BigDecimal endingCash
) {
    public enum CashFlowMethod {
        DIRECT, INDIRECT
    }

    public record CashFlowSectionDTO(
        String code,
        String name,
        ActivityType activityType,
        BigDecimal amount,
        List<CashFlowSectionDTO> children
    ) {
        public enum ActivityType {
            OPERATING, INVESTING, FINANCING
        }
    }
}