package com.erp.brazil.sales.infrastructure.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record SalesOrderCancelledEvent(
        UUID orderId,
        UUID companyId,
        String orderNumber,
        UUID customerId,
        String cancellationReason,
        LocalDateTime cancelledAt
) {}