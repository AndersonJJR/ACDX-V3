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

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferredEvent {

    private UUID transferId;
    private UUID companyId;
    private UUID itemId;
    private UUID itemVariantId;
    private UUID fromWarehouseId;
    private UUID toWarehouseId;
    private UUID fromLocationId;
    private UUID toLocationId;
    private java.math.BigDecimal qty;
    private java.math.BigDecimal valuationRate;
    private String referenceType;
    private String referenceNo;
    private UUID batchId;
    private UUID serialNumberId;
    private UUID outboundEntryId;
    private UUID inboundEntryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime timestamp;
}