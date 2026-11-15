package com.erp.brazil.sales.infrastructure.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StockReservedEvent(
        UUID reservationId,
        UUID orderId,
        UUID companyId,
        List<ReservationItemEvent> items,
        boolean success,
        String message
) {
    public record ReservationItemEvent(
            UUID productId,
            BigDecimal quantity,
            UUID warehouseId
    ) {}
}