package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.CommissionRule;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateCommissionRuleRequest(
        @Size(max = 20) String ruleCode,
        @Size(max = 100) String name,
        @Size(max = 1000) String description,
        CommissionRule.RuleType type,
        BigDecimal value,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        UUID productCategoryId,
        @Size(max = 50) String customerSegment,
        LocalDate validFrom,
        LocalDate validUntil,
        Boolean isActive,
        Integer priority,
        UUID salesTeamId,
        UUID salesPersonId,
        UUID territoryId
) {}