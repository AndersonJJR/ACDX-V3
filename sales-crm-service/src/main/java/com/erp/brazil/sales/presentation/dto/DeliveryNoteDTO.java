package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.DeliveryNoteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryNoteDTO(
        UUID id,
        UUID companyId,
        String number,
        String series,
        UUID salesOrderId,
        String salesOrderNumber,
        LocalDate deliveryDate,
        DeliveryNoteStatus status,
        String deliveryType,
        String carrier,
        String trackingCode,
        String vehiclePlate,
        String driverName,
        String driverDocument,
        BigDecimal freightValue,
        BigDecimal insuranceValue,
        BigDecimal totalWeight,
        BigDecimal totalVolume,
        String notes,
        LocalDateTime confirmedAt,
        UUID confirmedById,
        String confirmedByName,
        LocalDateTime cancelledAt,
        UUID cancelledById,
        String cancelledByName,
        String cancellationReason,
        UUID shippingAddressId,
        UUID carrierId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<DeliveryNoteItemDTO> items
) {
    public record DeliveryNoteItemDTO(
            UUID id,
            Integer lineNumber,
            UUID salesOrderItemId,
            UUID productId,
            String productCode,
            String productName,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal unitWeight,
            BigDecimal unitVolume,
            String batchNumber,
            String serialNumbers,
            String notes
    ) {}
}