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
public class StockBalanceResponse {

    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private String variantCode;
    private UUID warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private BigDecimal actualQty;
    private BigDecimal reservedQty;
    private BigDecimal availableQty;
    private BigDecimal orderedQty;
    private BigDecimal projectedQty;
    private BigDecimal valuationRate;
    private BigDecimal stockValue;
    private String valuationMethod;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastUpdated;
}