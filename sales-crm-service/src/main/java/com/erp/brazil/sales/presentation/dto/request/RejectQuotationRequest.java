package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RejectQuotationRequest(
        @NotNull UUID rejectedById,
        @NotBlank @Size(max = 1000) String rejectionReason
) {}