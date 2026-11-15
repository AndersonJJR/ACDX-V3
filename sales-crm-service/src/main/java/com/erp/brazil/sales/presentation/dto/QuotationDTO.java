package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.QuotationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuotationDTO(
        UUID id,
        UUID companyId,
        String number,
        String series,
        Integer version,
        UUID parentQuotationId,
        String parentQuotationNumber,
        String title,
        String description,
        QuotationStatus status,
        LocalDate validFrom,
        LocalDate validUntil,
        BigDecimal subtotal,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String currency,
        String paymentTerms,
        String deliveryTerms,
        String notes,
        String internalNotes,
        LocalDateTime sentAt,
        LocalDateTime acceptedAt,
        LocalDateTime rejectedAt,
        String rejectionReason,
        UUID opportunityId,
        String opportunityCode,
        UUID customerId,
        String customerName,
        UUID contactId,
        String contactName,
        UUID billingAddressId,
        UUID shippingAddressId,
        UUID createdById,
        String createdByName,
        UUID approvedById,
        String approvedByName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<QuotationItemDTO> items,
        List<QuotationVersionDTO> versions,
        Boolean hasSalesOrder
) {
    public record QuotationItemDTO(
            UUID id,
            Integer lineNumber,
            UUID productId,
            String productCode,
            String productName,
            String description,
            BigDecimal quantity,
            String unitOfMeasure,
            BigDecimal unitPrice,
            BigDecimal discountPercent,
            BigDecimal discountAmount,
            BigDecimal taxPercent,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            String notes
    ) {}

    public record QuotationVersionDTO(
            UUID id,
            Integer version,
            String number,
            QuotationStatus status,
            BigDecimal totalAmount,
            String changeReason,
            LocalDateTime createdAt,
            UUID createdById,
            String createdByName
    ) {}
}