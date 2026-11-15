package com.erp.brazil.finance.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceValidatedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID invoiceId,
        String invoiceNumber,
        String invoiceSeries,
        UUID vendorId,
        UUID customerId,
        LocalDate invoiceDate,
        LocalDate dueDate,
        BigDecimal totalAmount,
        BigDecimal taxAmount,
        String currencyCode,
        BigDecimal exchangeRate,
        String purchaseOrderNumber,
        String goodsReceiptNumber,
        String fiscalDocumentType,
        String accessKey
) {
}