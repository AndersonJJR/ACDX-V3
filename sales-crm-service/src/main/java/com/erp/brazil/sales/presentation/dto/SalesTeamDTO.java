package com.erp.brazil.sales.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SalesTeamDTO(
        UUID id,
        UUID companyId,
        String code,
        String name,
        String description,
        UUID managerId,
        String managerName,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SalesPersonDTO> members
) {}