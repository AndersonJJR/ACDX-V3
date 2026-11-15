package com.erp.brazil.sales.presentation.dto.request;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreatePipelineRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        Boolean isActive,
        Boolean isDefault,
        UUID salesTeamId,
        List<CreatePipelineStageRequest> stages
) {}