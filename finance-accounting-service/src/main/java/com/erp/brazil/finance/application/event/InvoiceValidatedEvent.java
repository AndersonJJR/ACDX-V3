package com.erp.brazil.finance.application.event;

import com.erp.brazil.finance.domain.model.InvoiceStatus;
import com.erp.brazil.finance.domain.model.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceValidatedEvent(
    UUID eventId,
    UUID invoiceId,
    UUID companyId,
    UUID vendorId,
    UUID customerId,
    PaymentType type,
    String number,
    String series,
    LocalDate issueDate,
    LocalDate dueDate,
    BigDecimal totalAmount,
    BigDecimal taxAmount,
    InvoiceStatus status,
    String xmlPath,
    String accessKey,
    UUID purchaseOrderId,
    UUID salesOrderId,
    UUID receiptId,
    LocalDateTime occurredAt
) {}