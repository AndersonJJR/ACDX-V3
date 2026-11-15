package com.erp.brazil.inventory.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
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
    private List<ItemVariantDTO> variants;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ItemVariantDTO {
    private UUID id;
    private String variantCode;
    private String variantName;
    private String attributes;
    private String barcode;
    private BigDecimal weight;
    private String weightUom;
    private BigDecimal volume;
    private String volumeUom;
    private BigDecimal standardRate;
    private Boolean isActive;
}