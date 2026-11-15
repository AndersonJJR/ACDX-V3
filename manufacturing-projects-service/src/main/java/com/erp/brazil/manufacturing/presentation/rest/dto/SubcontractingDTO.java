package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SubcontractingDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderRequest {
        private UUID vendorId;
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
        private UUID productId;
        private BigDecimal orderedQuantity;
        private String unitOfMeasure;
        private BigDecimal unitPrice;
        private LocalDateTime plannedEndDate;
        private UUID warehouseId;
        private String notes;
        private List<OrderItemRequest> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderResponse {
        private UUID id;
        private String orderNumber;
        private UUID vendorId;
        private UUID productionOrderId;
        private UUID productionOrderOperationId;
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
        private List<OrderItemResponse> items;
        private List<ReceiptResponse> receipts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        private UUID componentId;
        private String componentType;
        private BigDecimal quantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID locationId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private UUID id;
        private UUID subcontractingOrderId;
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiptRequest {
        private UUID subcontractingOrderId;
        private BigDecimal receivedQuantity;
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private UUID warehouseId;
        private UUID locationId;
        private String receiptNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiptResponse {
        private UUID id;
        private UUID subcontractingOrderId;
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
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SendMaterialsRequest {
        private List<MaterialShipment> shipments;
        private String shippedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaterialShipment {
        private UUID itemId;
        private BigDecimal quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InspectReceiptRequest {
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private UUID qualityInspectionId;
        private String inspectedBy;
    }
}