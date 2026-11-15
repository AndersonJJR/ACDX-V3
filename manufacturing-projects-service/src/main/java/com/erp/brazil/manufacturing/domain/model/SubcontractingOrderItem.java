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
@Table(name = "subcontracting_order_item", indexes = {
    @Index(name = "idx_sub_item_order", columnList = "subcontracting_order_id"),
    @Index(name = "idx_sub_item_component", columnList = "component_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcontractingOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcontracting_order_id", nullable = false)
    private SubcontractingOrder subcontractingOrder;

    @NotNull
    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "component_type", length = 50)
    private String componentType;

    @NotNull
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "sent_quantity", precision = 19, scale = 6)
    private BigDecimal sentQuantity = BigDecimal.ZERO;

    @Column(name = "returned_quantity", precision = 19, scale = 6)
    private BigDecimal returnedQuantity = BigDecimal.ZERO;

    @Column(name = "consumed_quantity", precision = 19, scale = 6)
    private BigDecimal consumedQuantity = BigDecimal.ZERO;

    @Column(name = "scrapped_quantity", precision = 19, scale = 6)
    private BigDecimal scrappedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

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

    public BigDecimal getRemainingToSend() {
        return quantity.subtract(sentQuantity);
    }

    public BigDecimal getRemainingToReturn() {
        return sentQuantity.subtract(returnedQuantity).subtract(consumedQuantity).subtract(scrappedQuantity);
    }
}