package com.erp.brazil.sales.presentation.dto;

import java.util.List;
import java.util.UUID;

public record PipelineKanbanDTO(
        UUID pipelineId,
        String pipelineName,
        List<KanbanColumnDTO> columns
) {}