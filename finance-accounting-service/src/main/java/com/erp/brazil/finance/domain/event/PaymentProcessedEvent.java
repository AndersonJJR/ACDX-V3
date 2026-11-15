package com.erp.brazil.finance.domain.event;

import com.erp.brazil.finance.domain.model.Payment;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID paymentId,
        String reference,
        com.erp.brazil.finance.domain.model.PaymentType paymentType,
        UUID vendorId,
        UUID customerId,
        UUID bankAccountId,
        LocalDate paymentDate,
        java.math.BigDecimal amount,
        java.math.BigDecimal baseAmount,
        String currencyCode,
        com.erp.brazil.finance.domain.model.PaymentStatus status,
        String authorizationCode
) {
    public static PaymentProcessedEvent from(Payment payment) {
        return new PaymentProcessedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                payment.getId(),
                payment.getReference(),
                payment.getPaymentType(),
                payment.getVendor() != null ? payment.getVendor().getId() : null,
                payment.getCustomer() != null ? payment.getCustomer().getId() : null,
                payment.getBankAccount().getId(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getBaseAmount(),
                payment.getCurrency().getCode(),
                payment.getStatus(),
                payment.getAuthorizationCode()
        );
    }
}