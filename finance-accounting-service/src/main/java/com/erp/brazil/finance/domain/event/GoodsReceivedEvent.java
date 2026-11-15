package com.erp.brazil.finance.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoodsReceivedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID goodsReceiptId,
        String goodsReceiptNumber,
        UUID purchaseOrderId,
        String purchaseOrderNumber,
        UUID vendorId,
        LocalDate receiptDate,
        BigDecimal totalAmount,
        String currencyCode,
        BigDecimal exchangeRate,
        UUID warehouseId,
        String warehouseCode
) {
}