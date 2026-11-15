package com.erp.brazil.sales.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateDeliveryNoteRequest(
        @NotNull LocalDate deliveryDate,
        @NotNull @Size(max = 20) String deliveryType,
        @Size(max = 100) String carrier,
        @Size(max = 100) String trackingCode,
        @Size(max = 10) String vehiclePlate,
        @Size(max = 100) String driverName,
        @Size(max = 20) String driverDocument,
        BigDecimal freightValue,
        BigDecimal insuranceValue,
        BigDecimal totalWeight,
        BigDecimal totalVolume,
        @Size(max = 2000) String notes,
        UUID shippingAddressId,
        UUID carrierId,
        @NotNull List<CreateDeliveryNoteItemRequest> items
) {}