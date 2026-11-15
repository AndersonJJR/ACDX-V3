package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOpportunityProductRequest(
        @NotNull UUID productId,
        @NotBlank @Size(max = 50) String productCode,
        @NotBlank @Size(max = 200) String productName,
        @NotNull BigDecimal quantity,
        @NotNull BigDecimal unitPrice,
        BigDecimal discountPercent,
        @Size(max = 500) String notes
) {}