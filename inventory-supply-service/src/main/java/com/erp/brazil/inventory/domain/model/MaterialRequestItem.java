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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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