package com.erp.brazil.sales.infrastructure.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryNoteCreatedEvent(
        UUID deliveryNoteId,
        UUID companyId,
        String deliveryNoteNumber,
        UUID salesOrderId,
        LocalDate deliveryDate,
        List<DeliveryItemEvent> items,
        LocalDateTime createdAt
) {
    public record DeliveryItemEvent(
            UUID productId,
            BigDecimal quantity
    ) {}
}