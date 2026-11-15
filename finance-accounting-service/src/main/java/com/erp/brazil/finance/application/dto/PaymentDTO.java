package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.PaymentStatus;
import com.erp.brazil.finance.domain.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PaymentDTO(
    UUID id,
    UUID companyId,
    @NotNull PaymentType type,
    @NotNull UUID methodId,
    @NotNull BigDecimal amount,
    @NotNull LocalDate paymentDate,
    @Size(max = 100) String reference,
    @Size(max = 500) String description,
    PaymentStatus status,
    @Size(max = 100) String transactionId,
    @Size(max = 500) String proofPath,
    List<PaymentAllocationDTO> allocations
) {}