package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AdvanceStageRequest(
        @NotNull UUID newStageId,
        @NotNull UUID movedById,
        Integer probability,
        @Size(max = 500) String notes
) {}