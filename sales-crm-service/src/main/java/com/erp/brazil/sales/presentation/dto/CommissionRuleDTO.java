package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.CommissionRule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommissionRuleDTO(
        UUID id,
        UUID companyId,
        String ruleCode,
        String name,
        String description,
        CommissionRule.RuleType type,
        BigDecimal value,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        UUID productCategoryId,
        String productCategoryName,
        String customerSegment,
        LocalDate validFrom,
        LocalDate validUntil,
        Boolean isActive,
        Integer priority,
        UUID salesTeamId,
        String salesTeamName,
        UUID salesPersonId,
        String salesPersonName,
        UUID territoryId,
        String territoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommissionTierDTO> tiers
) {
    public record CommissionTierDTO(
            UUID id,
            UUID commissionRuleId,
            Integer tierOrder,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal rate,
            BigDecimal fixedAmount
    ) {}
}