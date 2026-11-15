package com.erp.brazil.manufacturing.presentation.rest.dto;

import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
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
public class ProductionOrderDto {

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
    private List<ProductionOrderItemDto> items;
    private List<ProductionOrderOperationDto> operations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static ProductionOrderDto fromEntity(ProductionOrder po) {
        return ProductionOrderDto.builder()
                .id(po.getId())
                .orderNumber(po.getOrderNumber())
                .productId(po.getProductId())
                .orderType(po.getOrderType().name())
                .status(po.getStatus().name())
                .priority(po.getPriority().name())
                .description(po.getDescription())
                .orderedQuantity(po.getOrderedQuantity())
                .producedQuantity(po.getProducedQuantity())
                .scrappedQuantity(po.getScrappedQuantity())
                .unitOfMeasure(po.getUnitOfMeasure())
                .bomId(po.getBomId())
                .routingId(po.getRoutingId())
                .salesOrderId(po.getSalesOrderId())
                .salesOrderItemId(po.getSalesOrderItemId())
                .projectId(po.getProjectId())
                .warehouseId(po.getWarehouseId())
                .plannedStartDate(po.getPlannedStartDate())
                .plannedEndDate(po.getPlannedEndDate())
                .actualStartDate(po.getActualStartDate())
                .actualEndDate(po.getActualEndDate())
                .releasedDate(po.getReleasedDate())
                .completedDate(po.getCompletedDate())
                .totalSetupTimeMinutes(po.getTotalSetupTimeMinutes())
                .totalRunTimeMinutes(po.getTotalRunTimeMinutes())
                .totalCost(po.getTotalCost())
                .currency(po.getCurrency())
                .notes(po.getNotes())
                .items(po.getItems().stream().map(ProductionOrderItemDto::fromEntity).toList())
                .operations(po.getOperations().stream().map(ProductionOrderOperationDto::fromEntity).toList())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .createdBy(po.getCreatedBy())
                .updatedBy(po.getUpdatedBy())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionOrderItemDto {
        private UUID id;
        private UUID componentId;
        private String componentType;
        private BigDecimal requiredQuantity;
        private BigDecimal issuedQuantity;
        private BigDecimal consumedQuantity;
        private BigDecimal returnedQuantity;
        private BigDecimal scrappedQuantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID locationId;
        private UUID bomItemId;
        private String status;
        private String notes;

        public static ProductionOrderItemDto fromEntity(ProductionOrderItem item) {
            return ProductionOrderItemDto.builder()
                    .id(item.getId())
                    .componentId(item.getComponentId())
                    .componentType(item.getComponentType())
                    .requiredQuantity(item.getRequiredQuantity())
                    .issuedQuantity(item.getIssuedQuantity())
                    .consumedQuantity(item.getConsumedQuantity())
                    .returnedQuantity(item.getReturnedQuantity())
                    .scrappedQuantity(item.getScrappedQuantity())
                    .unitOfMeasure(item.getUnitOfMeasure())
                    .warehouseId(item.getWarehouseId())
                    .locationId(item.getLocationId())
                    .bomItemId(item.getBomItemId())
                    .status(item.getStatus() != null ? item.getStatus().name() : null)
                    .notes(item.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionOrderOperationDto {
        private UUID id;
        private Integer sequence;
        private UUID routingOperationId;
        private UUID workCenterId;
        private UUID workStationId;
        private String operationName;
        private String description;
        private BigDecimal plannedSetupTimeMinutes;
        private BigDecimal plannedRunTimeMinutes;
        private BigDecimal actualSetupTimeMinutes;
        private BigDecimal actualRunTimeMinutes;
        private Integer crewSize;
        private Boolean isOutsourced;
        private UUID vendorId;
        private UUID subcontractingOrderId;
        private String status;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private LocalDateTime actualStartDate;
        private LocalDateTime actualEndDate;
        private BigDecimal completedQuantity;
        private BigDecimal scrappedQuantity;
        private String notes;

        public static ProductionOrderOperationDto fromEntity(ProductionOrderOperation op) {
            return ProductionOrderOperationDto.builder()
                    .id(op.getId())
                    .sequence(op.getSequence())
                    .routingOperationId(op.getRoutingOperationId())
                    .workCenterId(op.getWorkCenterId())
                    .workStationId(op.getWorkStationId())
                    .operationName(op.getOperationName())
                    .description(op.getDescription())
                    .plannedSetupTimeMinutes(op.getPlannedSetupTimeMinutes())
                    .plannedRunTimeMinutes(op.getPlannedRunTimeMinutes())
                    .actualSetupTimeMinutes(op.getActualSetupTimeMinutes())
                    .actualRunTimeMinutes(op.getActualRunTimeMinutes())
                    .crewSize(op.getCrewSize())
                    .isOutsourced(op.getIsOutsourced())
                    .vendorId(op.getVendorId())
                    .subcontractingOrderId(op.getSubcontractingOrderId())
                    .status(op.getStatus().name())
                    .plannedStartDate(op.getPlannedStartDate())
                    .plannedEndDate(op.getPlannedEndDate())
                    .actualStartDate(op.getActualStartDate())
                    .actualEndDate(op.getActualEndDate())
                    .completedQuantity(op.getCompletedQuantity())
                    .scrappedQuantity(op.getScrappedQuantity())
                    .notes(op.getNotes())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProductionOrderRequest {
        private UUID productId;
        private BigDecimal orderedQuantity;
        private String unitOfMeasure;
        private String orderType;
        private String priority;
        private UUID salesOrderId;
        private UUID salesOrderItemId;
        private UUID projectId;
        private UUID warehouseId;
        private LocalDateTime plannedStartDate;
        private LocalDateTime plannedEndDate;
        private String description;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseProductionOrderRequest {
        private String releasedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportProductionRequest {
        private BigDecimal producedQuantity;
        private BigDecimal scrappedQuantity;
        private String notes;
        private String reportedBy;
    }
}