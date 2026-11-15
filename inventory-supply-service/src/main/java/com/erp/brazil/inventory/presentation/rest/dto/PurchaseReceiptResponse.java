package com.erp.brazil.inventory.presentation.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseReceiptResponse {

    private UUID id;
    private UUID companyId;
    private String receiptNo;
    private UUID supplierId;
    private String supplierName;
    private UUID purchaseOrderId;
    private String purchaseOrderNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate receiptDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate lrDate;

    private String vehicleNo;
    private String notes;
    private BigDecimal perBilled;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;

    private List<PurchaseReceiptItemResponse> items;

    // Static factory method
    public static PurchaseReceiptResponse of(UUID id, UUID companyId, String receiptNo, UUID supplierId, String supplierName,
            UUID purchaseOrderId, String purchaseOrderNo, LocalDate receiptDate, LocalDate postingDate,
            String status, String currency, BigDecimal exchangeRate, BigDecimal totalQty, BigDecimal totalAmount,
            BigDecimal taxAmount, BigDecimal grandTotal, Boolean inspectionRequired, String transporterName,
            String lrNo, LocalDate lrDate, String vehicleNo, String notes, BigDecimal perBilled,
            LocalDateTime createdAt, LocalDateTime updatedAt, List<PurchaseReceiptItemResponse> items) {
        PurchaseReceiptResponse response = new PurchaseReceiptResponse();
        response.id = id;
        response.companyId = companyId;
        response.receiptNo = receiptNo;
        response.supplierId = supplierId;
        response.supplierName = supplierName;
        response.purchaseOrderId = purchaseOrderId;
        response.purchaseOrderNo = purchaseOrderNo;
        response.receiptDate = receiptDate;
        response.postingDate = postingDate;
        response.status = status;
        response.currency = currency;
        response.exchangeRate = exchangeRate;
        response.totalQty = totalQty;
        response.totalAmount = totalAmount;
        response.taxAmount = taxAmount;
        response.grandTotal = grandTotal;
        response.inspectionRequired = inspectionRequired;
        response.transporterName = transporterName;
        response.lrNo = lrNo;
        response.lrDate = lrDate;
        response.vehicleNo = vehicleNo;
        response.notes = notes;
        response.perBilled = perBilled;
        response.createdAt = createdAt;
        response.updatedAt = updatedAt;
        response.items = items;
        return response;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public String getPurchaseOrderNo() { return purchaseOrderNo; }
    public void setPurchaseOrderNo(String purchaseOrderNo) { this.purchaseOrderNo = purchaseOrderNo; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public LocalDate getPostingDate() { return postingDate; }
    public void setPostingDate(LocalDate postingDate) { this.postingDate = postingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public Boolean getInspectionRequired() { return inspectionRequired; }
    public void setInspectionRequired(Boolean inspectionRequired) { this.inspectionRequired = inspectionRequired; }

    public String getTransporterName() { return transporterName; }
    public void setTransporterName(String transporterName) { this.transporterName = transporterName; }

    public String getLrNo() { return lrNo; }
    public void setLrNo(String lrNo) { this.lrNo = lrNo; }

    public LocalDate getLrDate() { return lrDate; }
    public void setLrDate(LocalDate lrDate) { this.lrDate = lrDate; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getPerBilled() { return perBilled; }
    public void setPerBilled(BigDecimal perBilled) { this.perBilled = perBilled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PurchaseReceiptItemResponse> getItems() { return items; }
    public void setItems(List<PurchaseReceiptItemResponse> items) { this.items = items; }
}