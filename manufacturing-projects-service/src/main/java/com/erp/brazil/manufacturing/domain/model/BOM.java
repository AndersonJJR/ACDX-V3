package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bom", indexes = {
    @Index(name = "idx_bom_product", columnList = "product_id"),
    @Index(name = "idx_bom_status", columnList = "status"),
    @Index(name = "idx_bom_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BOM {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private BOMType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BOMStatus status;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "base_quantity", precision = 19, scale = 6)
    private BigDecimal baseQuantity = BigDecimal.ONE;

    @Column(name = "scrap_percentage", precision = 5, scale = 2)
    private BigDecimal scrapPercentage = BigDecimal.ZERO;

    @Column(name = "is_phantom")
    private Boolean isPhantom = false;

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BOMItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BOMOperation> operations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Version
    private Long versionLock;

    public enum BOMType {
        PRODUCTION,
        ENGINEERING,
        SERVICE,
        PHANTOM
    }

    public enum BOMStatus {
        DRAFT,
        ACTIVE,
        OBSOLETE,
        ARCHIVED
    }

    public void addItem(BOMItem item) {
        items.add(item);
        item.setBom(this);
    }

    public void removeItem(BOMItem item) {
        items.remove(item);
        item.setBom(null);
    }

    public void addOperation(BOMOperation operation) {
        operations.add(operation);
        operation.setBom(this);
    }

    public void removeOperation(BOMOperation operation) {
        operations.remove(operation);
        operation.setBom(null);
    }
}