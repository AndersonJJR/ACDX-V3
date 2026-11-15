package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeliverItemsRequest(
        @NotNull UUID deliveredById,
        @NotNull List<DeliverItemQuantityRequest> items
) {}