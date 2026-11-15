package com.erp.brazil.manufacturing.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubcontractingOrderSentEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID subcontractingOrderId;
    private String orderNumber;
    private UUID vendorId;
    private List<SentItemEvent> sentItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentItemEvent {
        private UUID componentId;
        private BigDecimal sentQuantity;
        private String unitOfMeasure;
        private UUID fromWarehouseId;
        private UUID toWarehouseId;
    }
}