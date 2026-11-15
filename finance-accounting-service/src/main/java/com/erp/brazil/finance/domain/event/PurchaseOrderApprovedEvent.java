package com.erp.brazil.finance.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseOrderApprovedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID purchaseOrderId,
        String purchaseOrderNumber,
        UUID vendorId,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        BigDecimal totalAmount,
        String currencyCode,
        BigDecimal exchangeRate,
        UUID costCenterId,
        String costCenterCode,
        String approvalStatus,
        String approvedBy
) {
}