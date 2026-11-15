package com.erp.brazil.inventory.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBalanceDTO {
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private BigDecimal qty;
    private BigDecimal valuationRate;
    private BigDecimal stockValue;
    private String uom;
}