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
public class PurchaseInvoiceItemResponse {

    private UUID id;
    private UUID companyId;
    private UUID purchaseInvoiceId;
    private Integer idx;
    private UUID purchaseOrderItemId;
    private UUID purchaseReceiptItemId;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private String variantCode;
    private BigDecimal qty;
    private String uom;
    private BigDecimal conversionFactor;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private String taxTemplate;
    private BigDecimal taxAmount;
    private BigDecimal poQty;
    private BigDecimal receivedQty;
    private BigDecimal billedQty;
    private String matchStatus;
    private BigDecimal varianceQty;
    private BigDecimal varianceAmount;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;
}