package com.erp.brazil.finance.application.event;

import com.erp.brazil.finance.domain.model.PaymentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentProcessedEvent(
    UUID eventId,
    UUID paymentId,
    UUID companyId,
    PaymentType type,
    BigDecimal amount,
    LocalDate paymentDate,
    String reference,
    List<PaymentAllocationEvent> allocations,
    String processedBy,
    LocalDateTime occurredAt
) {
    public record PaymentAllocationEvent(
        UUID invoiceId,
        PaymentType invoiceType,
        BigDecimal amount
    ) {}
}