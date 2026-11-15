package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateQuotationItemRequest(
        @NotNull Integer lineNumber,
        @NotNull UUID productId,
        @NotBlank @Size(max = 50) String productCode,
        @NotBlank @Size(max = 200) String productName,
        @Size(max = 1000) String description,
        @NotNull BigDecimal quantity,
        @NotBlank @Size(max = 20) String unitOfMeasure,
        @NotNull BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal taxPercent,
        @Size(max = 500) String notes
) {}