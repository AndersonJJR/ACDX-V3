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
public class SubcontractingOrderCreatedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID subcontractingOrderId;
    private String orderNumber;
    private UUID vendorId;
    private UUID productionOrderId;
    private UUID productId;
    private BigDecimal orderedQuantity;
    private List<SubcontractItemEvent> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcontractItemEvent {
        private UUID componentId;
        private BigDecimal quantity;
        private String unitOfMeasure;
        private UUID warehouseId;
    }
}