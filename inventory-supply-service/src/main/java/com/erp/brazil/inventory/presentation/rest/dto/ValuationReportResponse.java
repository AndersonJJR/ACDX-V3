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
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValuationReportResponse {

    private UUID companyId;
    private UUID warehouseId;
    private String warehouseName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime asOfDate;

    private BigDecimal totalStockValue;
    private BigDecimal totalQty;
    private int totalItems;
    private List<ItemValuation> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemValuation {
        private UUID itemId;
        private String itemCode;
        private String itemName;
        private UUID itemVariantId;
        private String variantCode;
        private BigDecimal qty;
        private BigDecimal valuationRate;
        private BigDecimal stockValue;
        private String valuationMethod;
    }
}