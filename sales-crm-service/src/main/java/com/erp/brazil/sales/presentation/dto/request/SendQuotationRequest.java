package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SendQuotationRequest(
        @NotNull UUID sentById
) {}