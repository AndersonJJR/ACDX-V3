package com.erp.brazil.finance.application.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GoodsReceivedEvent(
    UUID eventId,
    UUID receiptId,
    UUID companyId,
    UUID purchaseOrderId,
    UUID vendorId,
    LocalDate receiptDate,
    List<GoodsReceivedLineEvent> lines,
    LocalDateTime occurredAt
) {
    public record GoodsReceivedLineEvent(
        UUID purchaseOrderLineId,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
    ) {}
}