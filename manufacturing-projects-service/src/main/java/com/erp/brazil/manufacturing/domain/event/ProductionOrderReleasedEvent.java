package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderReleasedEvent {

    private UUID eventId = UUID.randomUUID();
    private LocalDateTime timestamp = LocalDateTime.now();
    private String eventType = "ProductionOrderReleased";

    private UUID productionOrderId;
    private String orderNumber;
    private UUID productId;
    private java.math.BigDecimal orderedQuantity;
    private UUID warehouseId;
    private LocalDateTime plannedStartDate;
    private LocalDateTime plannedEndDate;
    private String releasedBy;
}