package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record QualifyLeadRequest(
        @NotNull UUID qualifiedById,
        Integer score,
        @Size(max = 1000) String notes
) {}