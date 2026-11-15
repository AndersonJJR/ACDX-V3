package com.erp.brazil.sales.infrastructure.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SalesOrderConfirmedEvent(
        UUID orderId,
        UUID companyId,
        String orderNumber,
        UUID customerId,
        BigDecimal totalAmount,
        List<OrderItemEvent> items,
        LocalDateTime confirmedAt
) {
    public record OrderItemEvent(
            UUID productId,
            BigDecimal quantity,
            UUID warehouseId
    ) {}
}