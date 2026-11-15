package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConvertLeadRequest(
        @NotNull UUID convertedById,
        @NotNull UUID pipelineId,
        @NotNull UUID stageId,
        @Size(max = 1000) String notes
) {}