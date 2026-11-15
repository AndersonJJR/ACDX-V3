package com.erp.brazil.manufacturing.domain.event;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceivedEvent {

    private UUID eventId;
    private LocalDateTime timestamp;
    private String eventType = "GoodsReceived";

    private UUID goodsReceiptId;
    private String receiptNumber;
    private UUID purchaseOrderId;
    private UUID productId;
    private String lotNumber;
    private java.math.BigDecimal receivedQuantity;
    private String unitOfMeasure;
    private UUID warehouseId;
    private UUID locationId;
    private LocalDateTime receiptDate;
    private String receivedBy;
}