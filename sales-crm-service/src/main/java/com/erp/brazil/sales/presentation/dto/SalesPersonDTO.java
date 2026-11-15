package com.erp.brazil.sales.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalesPersonDTO(
        UUID id,
        UUID companyId,
        String employeeId,
        String userId,
        String fullName,
        String email,
        String phone,
        String mobile,
        BigDecimal commissionRate,
        Boolean isActive,
        LocalDate hireDate,
        LocalDate terminationDate,
        UUID territoryId,
        String territoryName,
        UUID salesTeamId,
        String salesTeamName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}