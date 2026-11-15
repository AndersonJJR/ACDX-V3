package com.erp.brazil.manufacturing.presentation.rest.dto;

import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrderItem;
import com.erp.brazil.manufacturing.domain.model.SubcontractingReceipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubcontractingDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcontractingOrderDto {
        private UUID id;
        private String orderNumber;
        private UUID vendorId;
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
        private UUID routingOperationId;
        private UUID productId;
        private String status;
        private String description;
        private BigDecimal orderedQuantity;
        private BigDecimal receivedQuantity;
        private BigDecimal rejectedQuantity;
        private String unitOfMeasure;
        private BigDecimal unitPrice;
        private String currency;
        private BigDecimal totalAmount;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private UUID warehouseId;
        private String notes;
        private List<SubcontractingOrderItemDto> items;
        private List<SubcontractingReceiptDto> receipts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public static SubcontractingOrderDto fromEntity(SubcontractingOrder so) {
            return SubcontractingOrderDto.builder()
                    .id(so.getId())
                    .orderNumber(so.getOrderNumber())
                    .vendorId(so.getVendorId())
                    .productionOrderId(so.getProductionOrderId())
                    .productionOrderOperationId(so.getProductionOrderOperationId())
                    .routingOperationId(so.getRoutingOperationId())
                    .productId(so.getProductId())
                    .status(so.getStatus().name())
                    .description(so.getDescription())
                    .orderedQuantity(so.getOrderedQuantity())
                    .receivedQuantity(so.getReceivedQuantity())
                    .rejectedQuantity(so.getRejectedQuantity())
                    .unitOfMeasure(so.getUnitOfMeasure())
                    .unitPrice(so.getUnitPrice())
                    .currency(so.getCurrency())
                    .totalAmount(so.getTotalAmount())
                    .plannedStartDate(so.getPlannedStartDate())
                    .plannedEndDate(so.getPlannedEndDate())
                    .actualStartDate(so.getActualStartDate())
                    .actualEndDate(so.getActualEndDate())
                    .warehouseId(so.getWarehouseId())
                    .notes(so.getNotes())
                    .items(so.getItems().stream().map(SubcontractingOrderItemDto::fromEntity).toList())
                    .receipts(so.getReceipts().stream().map(SubcontractingReceiptDto::fromEntity).toList())
                    .createdAt(so.getCreatedAt())
                    .updatedAt(so.getUpdatedAt())
                    .createdBy(so.getCreatedBy())
                    .updatedBy(so.getUpdatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcontractingOrderItemDto {
        private UUID id;
        private UUID componentId;
        private String componentType;
        private BigDecimal quantity;
        private BigDecimal sentQuantity;
        private BigDecimal returnedQuantity;
        private BigDecimal consumedQuantity;
        private BigDecimal scrappedQuantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID locationId;
        private String notes;

        public static SubcontractingOrderItemDto fromEntity(SubcontractingOrderItem item) {
            return SubcontractingOrderItemDto.builder()
                    .id(item.getId())
                    .componentId(item.getComponentId())
                    .componentType(item.getComponentType())
                    .quantity(item.getQuantity())
                    .sentQuantity(item.getSentQuantity())
                    .returnedQuantity(item.getReturnedQuantity())
                    .consumedQuantity(item.getConsumedQuantity())
                    .scrappedQuantity(item.getScrappedQuantity())
                    .unitOfMeasure(item.getUnitOfMeasure())
                    .warehouseId(item.getWarehouseId())
                    .locationId(item.getLocationId())
                    .notes(item.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcontractingReceiptDto {
        private UUID id;
        private String receiptNumber;
        private LocalDateTime receiptDate;
        private UUID productId;
        private BigDecimal receivedQuantity;
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID locationId;
        private UUID qualityInspectionId;
        private String status;
        private String notes;
        private String vendorInvoiceNumber;
        private LocalDateTime vendorInvoiceDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;

        public static SubcontractingReceiptDto fromEntity(SubcontractingReceipt receipt) {
            return SubcontractingReceiptDto.builder()
                    .id(receipt.getId())
                    .receiptNumber(receipt.getReceiptNumber())
                    .receiptDate(receipt.getReceiptDate())
                    .productId(receipt.getProductId())
                    .receivedQuantity(receipt.getReceivedQuantity())
                    .acceptedQuantity(receipt.getAcceptedQuantity())
                    .rejectedQuantity(receipt.getRejectedQuantity())
                    .unitOfMeasure(receipt.getUnitOfMeasure())
                    .warehouseId(receipt.getWarehouseId())
                    .locationId(receipt.getLocationId())
                    .qualityInspectionId(receipt.getQualityInspectionId())
                    .status(receipt.getStatus().name())
                    .notes(receipt.getNotes())
                    .vendorInvoiceNumber(receipt.getVendorInvoiceNumber())
                    .vendorInvoiceDate(receipt.getVendorInvoiceDate())
                    .createdAt(receipt.getCreatedAt())
                    .updatedAt(receipt.getUpdatedAt())
                    .createdBy(receipt.getCreatedBy())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSubcontractingOrderRequest {
        private UUID productionOrderId;
        private UUID operationId;
        private UUID vendorId;
        private BigDecimal orderedQuantity;
        private BigDecimal unitPrice;
        private String currency;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private UUID warehouseId;
        private List<CreateSubcontractItemRequest> items;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSubcontractItemRequest {
        private UUID componentId;
        private String componentType;
        private BigDecimal quantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID locationId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMaterialsRequest {
        private String sentBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiveSubcontractedRequest {
        private String receiptNumber;
        private BigDecimal receivedQuantity;
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private UUID warehouseId;
        private UUID locationId;
        private String vendorInvoiceNumber;
        private LocalDateTime vendorInvoiceDate;
        private String notes;
        private String receivedBy;
    }
}