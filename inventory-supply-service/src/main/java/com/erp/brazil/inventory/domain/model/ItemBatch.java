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
@Table(name = "item_batches", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "batch_no"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class ItemBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id")
    private ItemVariant itemVariant;

    @Column(name = "batch_no", nullable = false, length = 50)
    private String batchNo;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity", precision = 20, scale = 6, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "valuation_rate", precision = 20, scale = 6)
    private BigDecimal valuationRate;

    @Column(name = "reference_doctype", length = 50)
    private String referenceDoctype;

    @Column(name = "reference_name", length = 100)
    private String referenceName;

    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "supplier_batch_no", length = 50)
    private String supplierBatchNo;

    @Column(name = "quality_status", length = 20)
    @Enumerated(EnumType.STRING)
    private QualityStatus qualityStatus = QualityStatus.PENDING;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemVariant getItemVariant() { return itemVariant; }
    public void setItemVariant(ItemVariant itemVariant) { this.itemVariant = itemVariant; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getValuationRate() { return valuationRate; }
    public void setValuationRate(BigDecimal valuationRate) { this.valuationRate = valuationRate; }

    public String getReferenceDoctype() { return referenceDoctype; }
    public void setReferenceDoctype(String referenceDoctype) { this.referenceDoctype = referenceDoctype; }

    public String getReferenceName() { return referenceName; }
    public void setReferenceName(String referenceName) { this.referenceName = referenceName; }

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public String getSupplierBatchNo() { return supplierBatchNo; }
    public void setSupplierBatchNo(String supplierBatchNo) { this.supplierBatchNo = supplierBatchNo; }

    public QualityStatus getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum QualityStatus {
        PENDING, ACCEPTED, REJECTED, QUARANTINE
    }
}