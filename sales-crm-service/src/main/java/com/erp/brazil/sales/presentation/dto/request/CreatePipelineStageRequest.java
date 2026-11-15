package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePipelineStageRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull Integer orderIndex,
        @NotNull PipelineStageType type,
        Integer probability,
        @Size(max = 7) String color,
        Boolean isActive,
        Integer maxDaysInStage,
        Boolean allowSkip
) {}