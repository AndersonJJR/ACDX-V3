package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateSalesInvoiceRequest(
        @NotNull LocalDate issueDate,
        LocalDate operationDate,
        @NotNull LocalDate dueDate,
        @NotBlank @Size(max = 20) String invoiceType,
        @NotBlank @Size(max = 60) String natureOfOperation,
        @Size(max = 100) String currency,
        UUID salesOrderId,
        UUID deliveryNoteId,
        @NotNull UUID customerId,
        UUID billingAddressId,
        @NotNull List<CreateSalesInvoiceItemRequest> items
) {}