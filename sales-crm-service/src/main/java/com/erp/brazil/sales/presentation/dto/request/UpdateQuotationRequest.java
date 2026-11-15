package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateQuotationRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        LocalDate validFrom,
        LocalDate validUntil,
        @Size(max = 100) String paymentTerms,
        @Size(max = 100) String deliveryTerms,
        @Size(max = 2000) String notes,
        @Size(max = 2000) String internalNotes,
        UUID opportunityId,
        UUID customerId,
        UUID contactId,
        UUID billingAddressId,
        UUID shippingAddressId,
        List<CreateQuotationItemRequest> items
) {}