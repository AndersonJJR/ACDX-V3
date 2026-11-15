package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull UUID companyId,
    @NotNull PaymentType type,
    @NotNull UUID methodId,
    @NotNull BigDecimal amount,
    @NotNull LocalDate paymentDate,
    @Size(max = 100) String reference,
    @Size(max = 500) String description,
    @NotNull @Valid List<CreatePaymentAllocationRequest> allocations
) {
    public record CreatePaymentAllocationRequest(
        @NotNull UUID invoiceId,
        @NotNull PaymentType invoiceType,
        @NotNull BigDecimal amount
    ) {}
}