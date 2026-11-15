package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateQuotationRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validUntil,
        @Size(max = 100) String paymentTerms,
        @Size(max = 100) String deliveryTerms,
        @Size(max = 2000) String notes,
        @Size(max = 2000) String internalNotes,
        UUID opportunityId,
        @NotNull UUID customerId,
        UUID contactId,
        UUID billingAddressId,
        UUID shippingAddressId,
        @NotNull List<CreateQuotationItemRequest> items
) {}