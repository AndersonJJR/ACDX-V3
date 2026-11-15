package com.erp.brazil.sales.infrastructure.event;

import java.util.UUID;

public record StockUnreservedEvent(
        UUID reservationId,
        UUID orderId,
        UUID companyId,
        String reason
) {}