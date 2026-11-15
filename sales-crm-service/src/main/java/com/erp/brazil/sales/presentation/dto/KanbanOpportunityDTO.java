package com.erp.brazil.sales.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record KanbanOpportunityDTO(
        UUID id,
        String opportunityCode,
        String title,
        BigDecimal amount,
        Integer probability,
        BigDecimal weightedAmount,
        LocalDateTime expectedCloseDate,
        String customerName,
        String salesPersonName,
        UUID stageId,
        String stageName,
        String stageColor
) {}