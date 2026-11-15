package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import java.util.List;
import java.util.UUID;

public record LeadKanbanColumnDTO(
        UUID stageId,
        String stageName,
        String stageColor,
        PipelineStageType stageType,
        Integer orderIndex,
        List<LeadKanbanCardDTO> leads,
        Integer leadCount
) {}