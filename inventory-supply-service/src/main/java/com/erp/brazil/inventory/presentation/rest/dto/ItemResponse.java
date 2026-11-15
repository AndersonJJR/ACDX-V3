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
public class ItemResponse {

    private UUID id;
    private UUID companyId;
    private String code;
    private String name;
    private String description;
    private String itemType;
    private String itemGroup;
    private String brand;
    private String unitOfMeasure;
    private String valuationMethod;
    private BigDecimal standardRate;
    private Boolean isActive;
    private Boolean isStockItem;
    private Boolean isPurchasable;
    private Boolean isSellable;
    private Boolean isManufactured;
    private Boolean hasBatch;
    private Boolean hasSerialNo;
    private String batchNumberSeries;
    private String serialNumberSeries;
    private UUID defaultWarehouseId;
    private BigDecimal minOrderQty;
    private BigDecimal maxOrderQty;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQty;
    private Integer leadTimeDays;
    private Integer shelfLifeDays;
    private String hsnCode;
    private String ncmCode;
    private String cestCode;
    private String origemMercadoria;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;
}