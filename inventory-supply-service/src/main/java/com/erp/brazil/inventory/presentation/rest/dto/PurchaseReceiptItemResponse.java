package com.erp.brazil.inventory.presentation.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PurchaseReceiptItemResponse {

    private UUID id;
    private UUID companyId;
    private UUID purchaseReceiptId;
    private Integer idx;
    private UUID purchaseOrderItemId;
    private UUID itemId;
    private String itemCode;
    private String itemName;
    private UUID itemVariantId;
    private String variantCode;
    private BigDecimal qty;
    private BigDecimal rejectedQty;
    private String uom;
    private BigDecimal conversionFactor;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private UUID warehouseId;
    private String warehouseCode;
    private UUID locationId;
    private String locationCode;
    private String batchNo;
    private String serialNos;
    private String qualityStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime manufacturingDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime expiryDate;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;

    // Static factory method
    public static PurchaseReceiptItemResponse of(UUID id, UUID companyId, UUID purchaseReceiptId, Integer idx,
            UUID purchaseOrderItemId, UUID itemId, String itemCode, String itemName, UUID itemVariantId, String variantCode,
            BigDecimal qty, BigDecimal rejectedQty, String uom, BigDecimal conversionFactor, BigDecimal rate,
            BigDecimal amount, BigDecimal taxAmount, UUID warehouseId, String warehouseCode, UUID locationId, String locationCode,
            String batchNo, String serialNos, String qualityStatus, LocalDateTime manufacturingDate,
            LocalDateTime expiryDate, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        PurchaseReceiptItemResponse response = new PurchaseReceiptItemResponse();
        response.id = id;
        response.companyId = companyId;
        response.purchaseReceiptId = purchaseReceiptId;
        response.idx = idx;
        response.purchaseOrderItemId = purchaseOrderItemId;
        response.itemId = itemId;
        response.itemCode = itemCode;
        response.itemName = itemName;
        response.itemVariantId = itemVariantId;
        response.variantCode = variantCode;
        response.qty = qty;
        response.rejectedQty = rejectedQty;
        response.uom = uom;
        response.conversionFactor = conversionFactor;
        response.rate = rate;
        response.amount = amount;
        response.taxAmount = taxAmount;
        response.warehouseId = warehouseId;
        response.warehouseCode = warehouseCode;
        response.locationId = locationId;
        response.locationCode = locationCode;
        response.batchNo = batchNo;
        response.serialNos = serialNos;
        response.qualityStatus = qualityStatus;
        response.manufacturingDate = manufacturingDate;
        response.expiryDate = expiryDate;
        response.description = description;
        response.createdAt = createdAt;
        response.updatedAt = updatedAt;
        return response;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public UUID getPurchaseReceiptId() { return purchaseReceiptId; }
    public void setPurchaseReceiptId(UUID purchaseReceiptId) { this.purchaseReceiptId = purchaseReceiptId; }

    public Integer getIdx() { return idx; }
    public void setIdx(Integer idx) { this.idx = idx; }

    public UUID getPurchaseOrderItemId() { return purchaseOrderItemId; }
    public void setPurchaseOrderItemId(UUID purchaseOrderItemId) { this.purchaseOrderItemId = purchaseOrderItemId; }

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

    public BigDecimal getRejectedQty() { return rejectedQty; }
    public void setRejectedQty(BigDecimal rejectedQty) { this.rejectedQty = rejectedQty; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public BigDecimal getConversionFactor() { return conversionFactor; }
    public void setConversionFactor(BigDecimal conversionFactor) { this.conversionFactor = conversionFactor; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getSerialNos() { return serialNos; }
    public void setSerialNos(String serialNos) { this.serialNos = serialNos; }

    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }

    public LocalDateTime getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDateTime manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}