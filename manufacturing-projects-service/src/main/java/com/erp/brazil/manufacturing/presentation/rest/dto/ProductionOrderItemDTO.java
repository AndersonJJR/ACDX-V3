package com.erp.brazil.manufacturing.presentation.rest.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductionOrderItemDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID componentId;
        private String componentType;
        private BigDecimal requiredQuantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID locationId;
        private UUID bomItemId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private UUID productionOrderId;
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}