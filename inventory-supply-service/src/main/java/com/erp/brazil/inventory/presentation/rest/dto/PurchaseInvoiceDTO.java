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
public class PurchaseInvoiceDTO {
    private UUID id;
    private UUID companyId;
    private String invoiceNo;
    private UUID supplierId;
    private String supplierName;
    private UUID purchaseOrderId;
    private String purchaseOrderNo;
    private UUID purchaseReceiptId;
    private String purchaseReceiptNo;
    private LocalDate invoiceDate;
    private LocalDate postingDate;
    private LocalDate dueDate;
    private String status;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal totalQty;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal advanceAllocated;
    private BigDecimal outstandingAmount;
    private String paymentTerms;
    private String nfeAccessKey;
    private Long nfeNumber;
    private String nfeSeries;
    private String nfeProtocol;
    private String nfeStatus;
    private String cfop;
    private String naturezaOperacao;
    private String notes;
    private Boolean isBlockedForPayment;
    private String blockReason;
    private List<PurchaseInvoiceItemDTO> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PurchaseInvoiceItemDTO {
    private UUID id;
    private Integer idx;
    private UUID purchaseOrderItemId;
    private UUID purchaseReceiptItemId;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
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
}