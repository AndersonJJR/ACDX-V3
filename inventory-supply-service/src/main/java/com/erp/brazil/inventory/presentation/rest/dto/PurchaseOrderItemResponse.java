package com.erp.brazil.inventory.presentation.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PurchaseOrderItemResponse {

    private UUID id;
    private UUID companyId;
    private UUID purchaseOrderId;
    private Integer idx;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private String variantCode;
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
    private BigDecimal pendingQty;
    private BigDecimal pendingBillingQty;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime requiredDate;

    private String description;
    private Boolean isSubcontracted;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;

    // Static factory method
    public static PurchaseOrderItemResponse of(UUID id, UUID companyId, UUID purchaseOrderId, Integer idx,
            UUID itemId, String itemCode, String itemName, UUID itemVariantId, String variantCode,
            BigDecimal qty, BigDecimal receivedQty, BigDecimal billedQty, String uom,
            BigDecimal conversionFactor, BigDecimal rate, BigDecimal amount,
            BigDecimal discountPercent, BigDecimal discountAmount, String taxTemplate,
            BigDecimal taxAmount, UUID warehouseId, BigDecimal pendingQty, BigDecimal pendingBillingQty,
            LocalDateTime requiredDate, String description, Boolean isSubcontracted,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        PurchaseOrderItemResponse response = new PurchaseOrderItemResponse();
        response.id = id;
        response.companyId = companyId;
        response.purchaseOrderId = purchaseOrderId;
        response.idx = idx;
        response.itemId = itemId;
        response.itemCode = itemCode;
        response.itemName = itemName;
        response.itemVariantId = itemVariantId;
        response.variantCode = variantCode;
        response.qty = qty;
        response.receivedQty = receivedQty;
        response.billedQty = billedQty;
        response.uom = uom;
        response.conversionFactor = conversionFactor;
        response.rate = rate;
        response.amount = amount;
        response.discountPercent = discountPercent;
        response.discountAmount = discountAmount;
        response.taxTemplate = taxTemplate;
        response.taxAmount = taxAmount;
        response.warehouseId = warehouseId;
        response.pendingQty = pendingQty;
        response.pendingBillingQty = pendingBillingQty;
        response.requiredDate = requiredDate;
        response.description = description;
        response.isSubcontracted = isSubcontracted;
        response.createdAt = createdAt;
        response.updatedAt = updatedAt;
        return response;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public Integer getIdx() { return idx; }
    public void setIdx(Integer idx) { this.idx = idx; }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public UUID getItemVariantId() { return itemVariantId; }
    public void setItemVariantId(UUID itemVariantId) { this.itemVariantId = itemVariantId; }

    public String getVariantCode() { return variantCode; }
    public void setVariantCode(String variantCode) { this.variantCode = variantCode; }

    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }

    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }

    public BigDecimal getBilledQty() { return billedQty; }
    public void setBilledQty(BigDecimal billedQty) { this.billedQty = billedQty; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public BigDecimal getConversionFactor() { return conversionFactor; }
    public void setConversionFactor(BigDecimal conversionFactor) { this.conversionFactor = conversionFactor; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public String getTaxTemplate() { return taxTemplate; }
    public void setTaxTemplate(String taxTemplate) { this.taxTemplate = taxTemplate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public BigDecimal getPendingQty() { return pendingQty; }
    public void setPendingQty(BigDecimal pendingQty) { this.pendingQty = pendingQty; }

    public BigDecimal getPendingBillingQty() { return pendingBillingQty; }
    public void setPendingBillingQty(BigDecimal pendingBillingQty) { this.pendingBillingQty = pendingBillingQty; }

    public LocalDateTime getRequiredDate() { return requiredDate; }
    public void setRequiredDate(LocalDateTime requiredDate) { this.requiredDate = requiredDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsSubcontracted() { return isSubcontracted; }
    public void setIsSubcontracted(Boolean isSubcontracted) { this.isSubcontracted = isSubcontracted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}