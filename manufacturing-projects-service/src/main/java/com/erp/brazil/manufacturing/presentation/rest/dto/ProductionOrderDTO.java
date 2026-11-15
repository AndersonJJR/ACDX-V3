package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProductionOrderDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID productId;
        private String orderType;
        private String priority;
        private String description;
        private BigDecimal orderedQuantity;
        private String unitOfMeasure;
        private UUID bomId;
        private UUID routingId;
        private UUID salesOrderId;
        private UUID salesOrderItemId;
        private UUID projectId;
        private UUID warehouseId;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private String notes;
        private List<ProductionOrderItemDTO.Request> items;
        private List<ProductionOrderOperationDTO.Request> operations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String orderNumber;
        private UUID productId;
        private String orderType;
        private String status;
        private String priority;
        private String description;
        private BigDecimal orderedQuantity;
        private BigDecimal producedQuantity;
        private BigDecimal scrappedQuantity;
        private String unitOfMeasure;
        private UUID bomId;
        private UUID routingId;
        private UUID salesOrderId;
        private UUID salesOrderItemId;
        private UUID projectId;
        private UUID warehouseId;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private LocalDateTime releasedDate;
        private LocalDateTime completedDate;
        private BigDecimal totalSetupTimeMinutes;
        private BigDecimal totalRunTimeMinutes;
        private BigDecimal totalCost;
        private String currency;
        private String notes;
        private List<ProductionOrderItemDTO.Response> items;
        private List<ProductionOrderOperationDTO.Response> operations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReleaseRequest {
        private String releasedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StartOperationRequest {
        private String startedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompleteOperationRequest {
        private BigDecimal completedQuantity;
        private BigDecimal scrappedQuantity;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private String completedBy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportProductionRequest {
        private BigDecimal quantityProduced;
        private BigDecimal quantityScrapped;
        private UUID warehouseId;
        private String reportedBy;
    }
}