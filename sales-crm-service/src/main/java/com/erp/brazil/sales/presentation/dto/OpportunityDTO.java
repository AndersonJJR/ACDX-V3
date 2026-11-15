package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.OpportunityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OpportunityDTO(
        UUID id,
        UUID companyId,
        String opportunityCode,
        String title,
        String description,
        OpportunityStatus status,
        BigDecimal amount,
        Integer probability,
        BigDecimal weightedAmount,
        LocalDateTime expectedCloseDate,
        LocalDateTime actualCloseDate,
        String lostReason,
        String lostToCompetitor,
        String notes,
        UUID leadId,
        String leadCode,
        UUID customerId,
        String customerName,
        UUID contactId,
        String contactName,
        UUID territoryId,
        String territoryName,
        UUID assignedSalesPersonId,
        String assignedSalesPersonName,
        UUID pipelineId,
        String pipelineName,
        UUID currentStageId,
        String currentStageName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OpportunityActivityDTO> activities,
        List<OpportunityProductDTO> products
) {
    public record OpportunityActivityDTO(
            UUID id,
            String type,
            String subject,
            String description,
            LocalDateTime scheduledAt,
            LocalDateTime completedAt,
            String outcome,
            UUID createdById,
            String createdByName,
            LocalDateTime createdAt
    ) {}

    public record OpportunityProductDTO(
            UUID id,
            UUID productId,
            String productCode,
            String productName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal discountPercent,
            BigDecimal totalAmount,
            String notes
    ) {}
}