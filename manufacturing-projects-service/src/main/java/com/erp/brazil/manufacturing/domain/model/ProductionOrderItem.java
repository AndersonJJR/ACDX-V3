package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "production_order_item", indexes = {
    @Index(name = "idx_po_item_prod_order", columnList = "production_order_id"),
    @Index(name = "idx_po_item_component", columnList = "component_id"),
    @Index(name = "idx_po_item_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @NotNull
    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "component_type", length = 50)
    private String componentType;

    @NotNull
    @Column(name = "required_quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal requiredQuantity;

    @Column(name = "issued_quantity", precision = 19, scale = 6)
    private BigDecimal issuedQuantity = BigDecimal.ZERO;

    @Column(name = "consumed_quantity", precision = 19, scale = 6)
    private BigDecimal consumedQuantity = BigDecimal.ZERO;

    @Column(name = "returned_quantity", precision = 19, scale = 6)
    private BigDecimal returnedQuantity = BigDecimal.ZERO;

    @Column(name = "scrapped_quantity", precision = 19, scale = 6)
    private BigDecimal scrappedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "bom_item_id")
    private UUID bomItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "notes", length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long versionLock;

    public enum ItemStatus {
        PENDING,
        PARTIAL,
        ISSUED,
        CONSUMED,
        COMPLETED,
        CANCELLED
    }

    public BigDecimal getRemainingQuantity() {
        return requiredQuantity.subtract(issuedQuantity);
    }

    public boolean isFullyIssued() {
        return issuedQuantity.compareTo(requiredQuantity) >= 0;
    }
}