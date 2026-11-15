package com.erp.brazil.inventory.presentation.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {
    private UUID id;
    private UUID companyId;
    private String poNo;
    private UUID supplierId;
    private String supplierName;
    private LocalDate poDate;
    private LocalDate expectedDeliveryDate;
    private String status;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal totalQty;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal advancePaid;
    private String paymentTerms;
    private String incoterm;
    private String shippingAddress;
    private String billingAddress;
    private String termsAndConditions;
    private String notes;
    private List<PurchaseOrderItemDTO> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PurchaseOrderItemDTO {
    private UUID id;
    private Integer idx;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private BigDecimal qty;
    private BigDecimal receivedQty;
    private BigDecimal billedQty;
    private String uom;
    private BigDecimal conversionFactor;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private String taxTemplate;
    private BigDecimal taxAmount;
    private UUID warehouseId;
    private String description;
    private Boolean isSubcontracted;
}