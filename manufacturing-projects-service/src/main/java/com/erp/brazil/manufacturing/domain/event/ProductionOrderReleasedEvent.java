package com.erp.brazil.manufacturing.domain.event;

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
public class ProductionOrderReleasedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID productionOrderId;
    private String orderNumber;
    private UUID productId;
    private BigDecimal orderedQuantity;
    private List<MaterialReservationItem> materialReservations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialReservationItem {
        private UUID componentId;
        private BigDecimal requiredQuantity;
        private String unitOfMeasure;
        private UUID warehouseId;
    }
}