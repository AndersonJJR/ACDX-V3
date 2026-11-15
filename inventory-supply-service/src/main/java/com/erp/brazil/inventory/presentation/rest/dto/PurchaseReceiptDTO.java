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
public class PurchaseReceiptDTO {
    private UUID id;
    private UUID companyId;
    private String receiptNo;
    private UUID supplierId;
    private String supplierName;
    private UUID purchaseOrderId;
    private String purchaseOrderNo;
    private LocalDate receiptDate;
    private LocalDate postingDate;
    private String status;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal totalQty;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private Boolean inspectionRequired;
    private String transporterName;
    private String lrNo;
    private LocalDate lrDate;
    private String vehicleNo;
    private String shippingAddress;
    private String notes;
    private BigDecimal perBilled;
    private List<PurchaseReceiptItemDTO> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PurchaseReceiptItemDTO {
    private UUID id;
    private Integer idx;
    private UUID purchaseOrderItemId;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private BigDecimal qty;
    private BigDecimal rejectedQty;
    private String uom;
    private BigDecimal conversionFactor;
    private BigDecimal rate;
    private BigDecimal amount;
    private UUID warehouseId;
    private String warehouseCode;
    private UUID locationId;
    private String batchNo;
    private String serialNos;
    private String qualityStatus;
    private String manufacturingDate;
    private String expiryDate;
    private String description;
}