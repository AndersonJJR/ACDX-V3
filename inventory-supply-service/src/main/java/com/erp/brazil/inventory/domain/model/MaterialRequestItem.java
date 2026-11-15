package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "material_request_items", indexes = {
    @Index(name = "idx_mri_mr", columnList = "material_request_id"),
    @Index(name = "idx_mri_item", columnList = "item_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class MaterialRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", nullable = false)
    private MaterialRequest materialRequest;

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

    @Column(name = "issued_qty", precision = 20, scale = 6)
    private BigDecimal issuedQty = BigDecimal.ZERO;

    @Column(name = "transferred_qty", precision = 20, scale = 6)
    private BigDecimal transferredQty = BigDecimal.ZERO;

    @Column(name = "uom", nullable = false, length = 20)
    private String uom;

    @Column(name = "conversion_factor", precision = 20, scale = 6)
    private BigDecimal conversionFactor = BigDecimal.ONE;

    @Column(name = "rate", precision = 20, scale = 6)
    private BigDecimal rate;

    @Column(name = "amount", precision = 20, scale = 6)
    private BigDecimal amount;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "from_warehouse_id")
    private UUID fromWarehouseId;

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

    public MaterialRequest getMaterialRequest() { return materialRequest; }
    public void setMaterialRequest(MaterialRequest materialRequest) { this.materialRequest = materialRequest; }

    public Integer getIdx() { return idx; }
    public void setIdx(Integer idx) { this.idx = idx; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public ItemVariant getItemVariant() { return itemVariant; }
    public void setItemVariant(ItemVariant itemVariant) { this.itemVariant = itemVariant; }

    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }

    public BigDecimal getIssuedQty() { return issuedQty; }
    public void setIssuedQty(BigDecimal issuedQty) { this.issuedQty = issuedQty; }

    public BigDecimal getTransferredQty() { return transferredQty; }
    public void setTransferredQty(BigDecimal transferredQty) { this.transferredQty = transferredQty; }

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

    public UUID getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(UUID fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}