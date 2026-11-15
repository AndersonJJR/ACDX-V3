package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LeadDTO(
        UUID id,
        UUID companyId,
        String leadCode,
        String title,
        String description,
        String source,
        Integer score,
        LeadStatus status,
        BigDecimal estimatedValue,
        Integer probability,
        LocalDateTime expectedCloseDate,
        LocalDateTime qualifiedAt,
        LocalDateTime convertedAt,
        String lostReason,
        String notes,
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
        List<LeadActivityDTO> activities
) {
    public record LeadActivityDTO(
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
}