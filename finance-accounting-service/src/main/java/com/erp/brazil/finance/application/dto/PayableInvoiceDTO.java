package com.erp.brazil.finance.application.dto;

import com.erp.brazil.finance.domain.model.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayableInvoiceDTO(
    UUID id,
    UUID companyId,
    @NotNull UUID vendorId,
    @NotBlank @Size(max = 50) String number,
    @Size(max = 10) String series,
    @NotNull LocalDate issueDate,
    @NotNull LocalDate dueDate,
    @NotNull BigDecimal totalAmount,
    BigDecimal taxAmount,
    InvoiceStatus status,
    @Size(max = 500) String xmlPath,
    @Size(max = 100) String accessKey,
    UUID purchaseOrderId,
    UUID receiptId,
    UUID paymentTermId,
    BigDecimal paidAmount,
    BigDecimal balanceAmount
) {}