package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcontractingOrderSentEvent {

    private UUID eventId = UUID.randomUUID();
    private LocalDateTime timestamp = LocalDateTime.now();
    private String eventType = "SubcontractingOrderSent";

    private UUID subcontractingOrderId;
    private String orderNumber;
    private UUID vendorId;
    private UUID productId;
    private java.math.BigDecimal orderedQuantity;
    private UUID warehouseId;
    private LocalDateTime plannedEndDate;
    private String sentBy;
}