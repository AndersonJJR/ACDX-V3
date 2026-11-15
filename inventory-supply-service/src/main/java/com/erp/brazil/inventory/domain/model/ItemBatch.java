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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public enum QualityStatus {
        PENDING, ACCEPTED, REJECTED, QUARANTINE
    }
}