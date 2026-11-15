package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "item_variants", uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "variant_code"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "variant_code", nullable = false, length = 50)
    private String variantCode;

    @Column(name = "variant_name", length = 100)
    private String variantName;

    @Column(name = "attributes", columnDefinition = "JSON")
    private String attributes;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "weight", precision = 20, scale = 6)
    private BigDecimal weight;

    @Column(name = "weight_uom", length = 10)
    private String weightUom;

    @Column(name = "volume", precision = 20, scale = 6)
    private BigDecimal volume;

    @Column(name = "volume_uom", length = 10)
    private String volumeUom;

    @Column(name = "length", precision = 20, scale = 6)
    private BigDecimal length;

    @Column(name = "width", precision = 20, scale = 6)
    private BigDecimal width;

    @Column(name = "height", precision = 20, scale = 6)
    private BigDecimal height;

    @Column(name = "dimension_uom", length = 10)
    private String dimensionUom;

    @Column(name = "standard_rate", precision = 20, scale = 6)
    private BigDecimal standardRate;

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

    public String getVariantCode() { return variantCode; }
    public void setVariantCode(String variantCode) { this.variantCode = variantCode; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getWeightUom() { return weightUom; }
    public void setWeightUom(String weightUom) { this.weightUom = weightUom; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public String getVolumeUom() { return volumeUom; }
    public void setVolumeUom(String volumeUom) { this.volumeUom = volumeUom; }

    public BigDecimal getLength() { return length; }
    public void setLength(BigDecimal length) { this.length = length; }

    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }

    public BigDecimal getHeight() { return height; }
    public void setHeight(BigDecimal height) { this.height = height; }

    public String getDimensionUom() { return dimensionUom; }
    public void setDimensionUom(String dimensionUom) { this.dimensionUom = dimensionUom; }

    public BigDecimal getStandardRate() { return standardRate; }
    public void setStandardRate(BigDecimal standardRate) { this.standardRate = standardRate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}