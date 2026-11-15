package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSalesPersonRequest(
        @NotBlank @Size(max = 50) String employeeId,
        @Size(max = 100) String userId,
        @NotBlank @Size(max = 200) String fullName,
        @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @Size(max = 20) String mobile,
        BigDecimal commissionRate,
        Boolean isActive,
        LocalDate hireDate,
        UUID territoryId,
        UUID salesTeamId
) {}