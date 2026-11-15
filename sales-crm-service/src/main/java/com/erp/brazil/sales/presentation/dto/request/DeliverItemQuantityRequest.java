package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliverItemQuantityRequest(
        @NotNull UUID itemId,
        @NotNull BigDecimal quantity
) {}