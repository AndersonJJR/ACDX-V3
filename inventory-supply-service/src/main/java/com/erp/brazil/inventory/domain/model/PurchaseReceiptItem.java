package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_receipt_items", indexes = {
    @Index(name = "idx_pri_receipt", columnList = "purchase_receipt_id"),
    @Index(name = "idx_pri_item", columnList = "item_id"),
    @Index(name = "idx_pri_po_item", columnList = "purchase_order_item_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class PurchaseReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_id", nullable = false)
    private PurchaseReceipt purchaseReceipt;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id")
    private PurchaseOrderItem purchaseOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id")
    private ItemVariant itemVariant;

    @Column(name = "qty", precision = 20, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "rejected_qty", precision = 20, scale = 6)
    private BigDecimal rejectedQty = BigDecimal.ZERO;

    @Column(name = "uom", nullable = false, length = 20)
    private String uom;

    @Column(name = "conversion_factor", precision = 20, scale = 6)
    private BigDecimal conversionFactor = BigDecimal.ONE;

    @Column(name = "rate", precision = 20, scale = 6)
    private BigDecimal rate;

    @Column(name = "amount", precision = 20, scale = 6)
    private BigDecimal amount;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "serial_nos", columnDefinition = "TEXT")
    private String serialNos;

    @Column(name = "quality_status", length = 20)
    @Enumerated(EnumType.STRING)
    private ItemBatch.QualityStatus qualityStatus = ItemBatch.QualityStatus.PENDING;

    @Column(name = "manufacturing_date")
    private LocalDateTime manufacturingDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

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

    public PurchaseReceipt getPurchaseReceipt() { return purchaseReceipt; }
    public void setPurchaseReceipt(PurchaseReceipt purchaseReceipt) { this.purchaseReceipt = purchaseReceipt; }

    public Integer getIdx() { return idx; }
    public void setIdx(Integer idx) { this.idx = idx; }

    public PurchaseOrderItem getPurchaseOrderItem() { return purchaseOrderItem; }
    public void setPurchaseOrderItem(PurchaseOrderItem purchaseOrderItem) { this.purchaseOrderItem = purchaseOrderItem; }

    public UUID getPurchaseOrderItemId() { return purchaseOrderItemId; }
    public void setPurchaseOrderItemId(UUID purchaseOrderItemId) { this.purchaseOrderItemId = purchaseOrderItemId; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemVariant getItemVariant() { return itemVariant; }
    public void setItemVariant(ItemVariant itemVariant) { this.itemVariant = itemVariant; }

    public UUID getItemVariantId() { return itemVariantId; }
    public void setItemVariantId(UUID itemVariantId) { this.itemVariantId = itemVariantId; }

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

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getSerialNos() { return serialNos; }
    public void setSerialNos(String serialNos) { this.serialNos = serialNos; }

    public ItemBatch.QualityStatus getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(ItemBatch.QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; }

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}