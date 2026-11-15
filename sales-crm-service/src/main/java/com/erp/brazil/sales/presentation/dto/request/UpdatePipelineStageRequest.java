package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import jakarta.validation.constraints.Size;

public record UpdatePipelineStageRequest(
        @Size(max = 20) String code,
        @Size(max = 100) String name,
        @Size(max = 500) String description,
        Integer orderIndex,
        PipelineStageType type,
        Integer probability,
        @Size(max = 7) String color,
        Boolean isActive,
        Integer maxDaysInStage,
        Boolean allowSkip
) {}