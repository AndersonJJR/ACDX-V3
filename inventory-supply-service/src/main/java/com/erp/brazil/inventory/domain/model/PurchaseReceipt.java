package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_receipts", indexes = {
    @Index(name = "idx_pr_company_no", columnList = "company_id, receipt_no", unique = true),
    @Index(name = "idx_pr_supplier", columnList = "supplier_id"),
    @Index(name = "idx_pr_status", columnList = "status"),
    @Index(name = "idx_pr_date", columnList = "receipt_date"),
    @Index(name = "idx_pr_po", columnList = "purchase_order_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class PurchaseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "receipt_no", nullable = false, length = 50)
    private String receiptNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PRStatus status = PRStatus.DRAFT;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "exchange_rate", precision = 20, scale = 6)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "total_qty", precision = 20, scale = 6)
    private BigDecimal totalQty;

    @Column(name = "total_amount", precision = 20, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 20, scale = 6)
    private BigDecimal taxAmount;

    @Column(name = "grand_total", precision = 20, scale = 6)
    private BigDecimal grandTotal;

    @Column(name = "inspection_required", nullable = false)
    private Boolean inspectionRequired = false;

    @Column(name = "inspection_by")
    private UUID inspectionBy;

    @Column(name = "inspection_date")
    private LocalDateTime inspectionDate;

    @Column(name = "transporter_name", length = 100)
    private String transporterName;

    @Column(name = "lr_no", length = 50)
    private String lrNo;

    @Column(name = "lr_date")
    private LocalDate lrDate;

    @Column(name = "vehicle_no", length = 20)
    private String vehicleNo;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "per_billed", precision = 10, scale = 2)
    private BigDecimal perBilled = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    public LocalDate getPostingDate() { return postingDate; }
    public void setPostingDate(LocalDate postingDate) { this.postingDate = postingDate; }

    public PRStatus getStatus() { return status; }
    public void setStatus(PRStatus status) { this.status = status; }

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

    public UUID getInspectionBy() { return inspectionBy; }
    public void setInspectionBy(UUID inspectionBy) { this.inspectionBy = inspectionBy; }

    public LocalDateTime getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDateTime inspectionDate) { this.inspectionDate = inspectionDate; }

    public String getTransporterName() { return transporterName; }
    public void setTransporterName(String transporterName) { this.transporterName = transporterName; }

    public String getLrNo() { return lrNo; }
    public void setLrNo(String lrNo) { this.lrNo = lrNo; }

    public LocalDate getLrDate() { return lrDate; }
    public void setLrDate(LocalDate lrDate) { this.lrDate = lrDate; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getPerBilled() { return perBilled; }
    public void setPerBilled(BigDecimal perBilled) { this.perBilled = perBilled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum PRStatus {
        DRAFT, SUBMITTED, QUALITY_INSPECTION, ACCEPTED, REJECTED, PARTIALLY_RECEIVED, COMPLETED, RETURNED, CANCELLED
    }

    // Static factory method for creating new instances
    public static PurchaseReceipt create(UUID companyId, String receiptNo, Supplier supplier,
            PurchaseOrder purchaseOrder, LocalDate receiptDate, LocalDate postingDate,
            String currency, BigDecimal exchangeRate, Boolean inspectionRequired,
            String transporterName, String lrNo, LocalDate lrDate, String vehicleNo,
            String shippingAddress, String notes) {
        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setCompanyId(companyId);
        receipt.setReceiptNo(receiptNo);
        receipt.setSupplier(supplier);
        receipt.setPurchaseOrder(purchaseOrder);
        receipt.setReceiptDate(receiptDate);
        receipt.setPostingDate(postingDate);
        receipt.setStatus(PRStatus.DRAFT);
        receipt.setCurrency(currency);
        receipt.setExchangeRate(exchangeRate);
        receipt.setInspectionRequired(inspectionRequired);
        receipt.setTransporterName(transporterName);
        receipt.setLrNo(lrNo);
        receipt.setLrDate(lrDate);
        receipt.setVehicleNo(vehicleNo);
        receipt.setShippingAddress(shippingAddress);
        receipt.setNotes(notes);
        return receipt;
    }
}