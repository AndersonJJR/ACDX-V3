package com.erp.brazil.sales.presentation.dto;

import java.util.List;
import java.util.UUID;

public record LeadKanbanDTO(
        UUID pipelineId,
        String pipelineName,
        List<LeadKanbanColumnDTO> columns
) {}