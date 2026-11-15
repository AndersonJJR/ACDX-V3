package com.erp.brazil.finance.application.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderApprovedEvent(
    UUID eventId,
    UUID purchaseOrderId,
    UUID companyId,
    UUID vendorId,
    LocalDate orderDate,
    LocalDate expectedDate,
    BigDecimal totalAmount,
    List<PurchaseOrderLineEvent> lines,
    LocalDateTime occurredAt
) {
    public record PurchaseOrderLineEvent(
        UUID lineId,
        UUID productId,
        UUID costCenterId,
        UUID accountId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
    ) {}
}