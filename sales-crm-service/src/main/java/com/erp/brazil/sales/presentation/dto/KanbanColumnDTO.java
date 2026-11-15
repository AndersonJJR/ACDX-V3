package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record KanbanColumnDTO(
        UUID stageId,
        String stageName,
        String stageColor,
        PipelineStageType stageType,
        Integer orderIndex,
        List<KanbanOpportunityDTO> opportunities,
        Integer opportunityCount,
        BigDecimal totalAmount,
        BigDecimal totalWeightedAmount
) {}