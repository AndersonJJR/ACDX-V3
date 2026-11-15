package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_items", indexes = {
    @Index(name = "idx_po_item_po", columnList = "purchase_order_id"),
    @Index(name = "idx_po_item_item", columnList = "item_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_variant_id")
    private ItemVariant itemVariant;

    @Column(name = "qty", precision = 20, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "received_qty", precision = 20, scale = 6)
    private BigDecimal receivedQty = BigDecimal.ZERO;

    @Column(name = "billed_qty", precision = 20, scale = 6)
    private BigDecimal billedQty = BigDecimal.ZERO;

    @Column(name = "uom", nullable = false, length = 20)
    private String uom;

    @Column(name = "conversion_factor", precision = 20, scale = 6)
    private BigDecimal conversionFactor = BigDecimal.ONE;

    @Column(name = "rate", precision = 20, scale = 6, nullable = false)
    private BigDecimal rate;

    @Column(name = "amount", precision = 20, scale = 6)
    private BigDecimal amount;

    @Column(name = "discount_percent", precision = 10, scale = 4)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 20, scale = 6)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_template", length = 50)
    private String taxTemplate;

    @Column(name = "tax_amount", precision = 20, scale = 6)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "required_date")
    private LocalDateTime requiredDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_subcontracted", nullable = false)
    private Boolean isSubcontracted = false;

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

    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public Integer getIdx() { return idx; }
    public void setIdx(Integer idx) { this.idx = idx; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemVariant getItemVariant() { return itemVariant; }
    public void setItemVariant(ItemVariant itemVariant) { this.itemVariant = itemVariant; }

    public UUID getItemVariantId() { return itemVariantId; }
    public void setItemVariantId(UUID itemVariantId) { this.itemVariantId = itemVariantId; }

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}