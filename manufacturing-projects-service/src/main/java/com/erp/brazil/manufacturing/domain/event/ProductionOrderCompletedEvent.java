package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderCompletedEvent {

    private UUID eventId = UUID.randomUUID();
    private LocalDateTime timestamp = LocalDateTime.now();
    private String eventType = "ProductionOrderCompleted";

    private UUID productionOrderId;
    private String orderNumber;
    private UUID productId;
    private java.math.BigDecimal producedQuantity;
    private java.math.BigDecimal scrappedQuantity;
    private UUID warehouseId;
    private LocalDateTime completedDate;
    private String completedBy;
}