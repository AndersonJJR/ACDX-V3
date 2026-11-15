package com.erp.brazil.manufacturing.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderCompletedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID productionOrderId;
    private String orderNumber;
    private UUID productId;
    private BigDecimal producedQuantity;
    private BigDecimal scrappedQuantity;
    private UUID warehouseId;
}