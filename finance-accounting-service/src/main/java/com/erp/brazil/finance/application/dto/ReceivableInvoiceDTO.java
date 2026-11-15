package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceivableInvoiceDTO(
    UUID id,
    UUID companyId,
    @NotNull UUID customerId,
    @NotBlank @Size(max = 50) String number,
    @Size(max = 10) String series,
    @NotNull LocalDate issueDate,
    @NotNull LocalDate dueDate,
    @NotNull BigDecimal totalAmount,
    BigDecimal taxAmount,
    InvoiceStatus status,
    @Size(max = 500) String xmlPath,
    @Size(max = 100) String accessKey,
    UUID salesOrderId,
    UUID paymentTermId,
    BigDecimal paidAmount,
    BigDecimal balanceAmount
) {}