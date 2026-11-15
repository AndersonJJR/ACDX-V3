package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.CommissionRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateCommissionRuleRequest(
        @NotBlank @Size(max = 20) String ruleCode,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description,
        @NotNull CommissionRule.RuleType type,
        BigDecimal value,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        UUID productCategoryId,
        @Size(max = 50) String customerSegment,
        @NotNull LocalDate validFrom,
        LocalDate validUntil,
        Boolean isActive,
        Integer priority,
        UUID salesTeamId,
        UUID salesPersonId,
        UUID territoryId,
        List<CreateCommissionTierRequest> tiers
) {}