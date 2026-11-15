package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderConfirmedEvent {

    private UUID eventId;
    private LocalDateTime timestamp;
    private String eventType = "SalesOrderConfirmed";

    private UUID salesOrderId;
    private String orderNumber;
    private UUID customerId;
    private LocalDateTime orderDate;
    private LocalDateTime requestedDeliveryDate;
    private List<SalesOrderItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesOrderItem {
        private UUID salesOrderItemId;
        private UUID productId;
        private java.math.BigDecimal quantity;
        private String unitOfMeasure;
        private UUID warehouseId;
        private UUID projectId;
    }
}