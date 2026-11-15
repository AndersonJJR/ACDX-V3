package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDeliveryNoteItemRequest(
        @NotNull Integer lineNumber,
        @NotNull UUID salesOrderItemId,
        @NotNull UUID productId,
        @NotBlank @Size(max = 50) String productCode,
        @NotBlank @Size(max = 200) String productName,
        @NotNull BigDecimal quantity,
        @NotBlank @Size(max = 20) String unitOfMeasure,
        BigDecimal unitWeight,
        BigDecimal unitVolume,
        @Size(max = 50) String batchNumber,
        @Size(max = 500) String serialNumbers,
        @Size(max = 500) String notes
) {}