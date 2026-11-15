package com.erp.brazil.inventory.presentation.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
public class StockLedgerEntryResponse {

    private UUID id;
    private UUID companyId;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private String variantCode;
    private UUID warehouseId;
    private String warehouseCode;
    private UUID locationId;
    private String locationCode;
    private UUID batchId;
    private String batchNo;
    private UUID serialNumberId;
    private String serialNo;
    private BigDecimal qtyChange;
    private BigDecimal valuationRate;
    private BigDecimal stockValue;
    private BigDecimal stockQtyAfter;
    private BigDecimal stockValueAfter;
    private String voucherType;
    private String voucherNo;
    private String voucherDetailNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime postingDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime postingTime;

    private BigDecimal actualQty;
    private BigDecimal incomingRate;
    private BigDecimal outgoingRate;
    private String valuationMethod;
    private Boolean isCancelled;
    private Boolean isReposted;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;
}