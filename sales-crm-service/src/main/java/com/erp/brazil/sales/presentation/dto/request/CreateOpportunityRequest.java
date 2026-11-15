package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateOpportunityRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @NotNull BigDecimal amount,
        Integer probability,
        LocalDateTime expectedCloseDate,
        @Size(max = 1000) String notes,
        UUID leadId,
        @NotNull UUID customerId,
        UUID contactId,
        UUID territoryId,
        UUID assignedSalesPersonId,
        UUID pipelineId,
        UUID currentStageId,
        List<CreateOpportunityProductRequest> products
) {}