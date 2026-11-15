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
public class SubcontractingReceivedEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private UUID subcontractingReceiptId;
    private String receiptNumber;
    private UUID subcontractingOrderId;
    private UUID productId;
    private BigDecimal receivedQuantity;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private UUID warehouseId;
}