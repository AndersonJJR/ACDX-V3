package com.erp.brazil.sales.infrastructure.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalesInvoiceCreatedEvent(
        UUID invoiceId,
        UUID companyId,
        String invoiceNumber,
        String accessKey,
        UUID customerId,
        BigDecimal totalAmount,
        LocalDateTime authorizedAt
) {}