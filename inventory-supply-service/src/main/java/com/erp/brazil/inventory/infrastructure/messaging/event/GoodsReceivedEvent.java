package com.erp.brazil.inventory.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceivedEvent {

    private UUID receiptId;
    private UUID companyId;
    private UUID supplierId;
    private String receiptNo;
    private LocalDate receiptDate;
    private UUID purchaseOrderId;
    private List<ItemDetail> items;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDetail {
        private UUID itemId;
        private UUID itemVariantId;
        private UUID warehouseId;
        private java.math.BigDecimal qty;
        private java.math.BigDecimal valuationRate;
        private UUID batchId;
        private UUID serialNumberId;
    }
}