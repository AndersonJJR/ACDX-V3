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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}