package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CloseOpportunityRequest(
        @NotNull UUID closedById,
        boolean won,
        @Size(max = 500) String lostReason,
        @Size(max = 100) String lostToCompetitor,
        @Size(max = 1000) String notes
) {}