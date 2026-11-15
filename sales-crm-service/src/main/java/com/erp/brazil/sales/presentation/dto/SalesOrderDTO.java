package com.erp.brazil.sales.presentation.dto;

import com.erp.brazil.sales.domain.model.SalesOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SalesOrderDTO(
        UUID id,
        UUID companyId,
        String number,
        String series,
        UUID quotationId,
        String quotationNumber,
        SalesOrderStatus status,
        LocalDate orderDate,
        LocalDate deliveryDate,
        LocalDateTime confirmedAt,
        UUID confirmedById,
        String confirmedByName,
        BigDecimal subtotal,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal shippingAmount,
        String currency,
        String paymentTerms,
        String deliveryTerms,
        String notes,
        String internalNotes,
        LocalDateTime cancelledAt,
        UUID cancelledById,
        String cancelledByName,
        String cancellationReason,
        UUID opportunityId,
        String opportunityCode,
        UUID customerId,
        String customerName,
        UUID contactId,
        String contactName,
        UUID billingAddressId,
        UUID shippingAddressId,
        UUID salesPersonId,
        String salesPersonName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SalesOrderItemDTO> items,
        List<DeliveryNoteDTO> deliveryNotes,
        List<SalesInvoiceDTO> invoices
) {
    public record SalesOrderItemDTO(
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
            BigDecimal deliveredQuantity,
            BigDecimal pendingQuantity,
            String notes
    ) {}

    public record DeliveryNoteDTO(
            UUID id,
            String number,
            String series,
            LocalDate deliveryDate,
            com.erp.brazil.sales.domain.model.DeliveryNoteStatus status,
            BigDecimal totalAmount
    ) {}

    public record SalesInvoiceDTO(
            UUID id,
            String number,
            String series,
            com.erp.brazil.sales.domain.model.InvoiceStatus status,
            LocalDate issueDate,
            BigDecimal totalAmount
    ) {}
}