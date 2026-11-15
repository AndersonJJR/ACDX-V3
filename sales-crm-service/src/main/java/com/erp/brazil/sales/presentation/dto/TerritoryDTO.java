package com.erp.brazil.sales.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TerritoryDTO(
        UUID id,
        UUID companyId,
        String code,
        String name,
        String description,
        String state,
        List<String> cities,
        List<String> zipCodeRanges,
        Boolean active,
        UUID parentTerritoryId,
        String parentTerritoryName,
        List<TerritoryDTO> subTerritories,
        Long customerCount,
        Long leadCount,
        Long opportunityCount,
        Long salesPersonCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}