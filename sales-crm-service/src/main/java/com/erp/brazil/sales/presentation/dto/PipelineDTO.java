package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.PipelineStageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PipelineDTO(
        UUID id,
        UUID companyId,
        String code,
        String name,
        String description,
        Boolean isActive,
        Boolean isDefault,
        UUID salesTeamId,
        String salesTeamName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PipelineStageDTO> stages
) {
    public record PipelineStageDTO(
            UUID id,
            UUID pipelineId,
            String code,
            String name,
            String description,
            Integer orderIndex,
            PipelineStageType type,
            Integer probability,
            String color,
            Boolean isActive,
            Integer maxDaysInStage,
            Boolean allowSkip,
            Long opportunityCount,
            BigDecimal totalAmount
    ) {}
}