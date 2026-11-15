package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAllocationDTO(
    UUID id,
    UUID paymentId,
    @NotNull UUID invoiceId,
    @NotNull PaymentType invoiceType,
    @NotNull BigDecimal amount
) {}