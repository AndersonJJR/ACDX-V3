package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateLeadRequest(
        @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @Size(max = 50) String source,
        @Size(max = 1000) String notes,
        BigDecimal estimatedValue,
        Integer probability,
        LocalDateTime expectedCloseDate,
        UUID customerId,
        UUID contactId,
        UUID territoryId,
        UUID assignedSalesPersonId,
        UUID pipelineId,
        UUID currentStageId
) {}