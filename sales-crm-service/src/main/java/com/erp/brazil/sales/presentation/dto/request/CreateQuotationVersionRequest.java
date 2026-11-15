package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateQuotationVersionRequest(
        @NotBlank @Size(max = 500) String changeReason,
        @NotNull List<CreateQuotationItemRequest> items
) {}