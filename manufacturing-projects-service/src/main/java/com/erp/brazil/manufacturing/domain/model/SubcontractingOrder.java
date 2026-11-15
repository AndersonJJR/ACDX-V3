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
@Table(name = "subcontracting_order", indexes = {
    @Index(name = "idx_sub_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_sub_order_vendor", columnList = "vendor_id"),
    @Index(name = "idx_sub_order_status", columnList = "status"),
    @Index(name = "idx_sub_order_dates", columnList = "planned_start_date,planned_end_date"),
    @Index(name = "idx_sub_order_prod_order", columnList = "production_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcontractingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull
    @Column(name = "vendor_id", nullable = false)
    private UUID vendorId;

    @Column(name = "production_order_id")
    private UUID productionOrderId;

    @Column(name = "production_order_operation_id")
    private UUID productionOrderOperationId;

    @Column(name = "routing_operation_id")
    private UUID routingOperationId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubcontractingStatus status = SubcontractingStatus.DRAFT;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Column(name = "ordered_quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal orderedQuantity;

    @Column(name = "received_quantity", precision = 19, scale = 6)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity", precision = 19, scale = 6)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "subcontractingOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubcontractingOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "subcontractingOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SubcontractingReceipt> receipts = new ArrayList<>();

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

    public enum SubcontractingStatus {
        DRAFT,
        SENT,
        ACKNOWLEDGED,
        IN_PROGRESS,
        PARTIAL_RECEIPT,
        RECEIVED,
        COMPLETED,
        CANCELLED,
        CLOSED
    }

    public void addItem(SubcontractingOrderItem item) {
        items.add(item);
        item.setSubcontractingOrder(this);
    }

    public void removeItem(SubcontractingOrderItem item) {
        items.remove(item);
        item.setSubcontractingOrder(null);
    }

    public void addReceipt(SubcontractingReceipt receipt) {
        receipts.add(receipt);
        receipt.setSubcontractingOrder(this);
    }

    public void removeReceipt(SubcontractingReceipt receipt) {
        receipts.remove(receipt);
        receipt.setSubcontractingOrder(null);
    }

    public BigDecimal getRemainingQuantity() {
        return orderedQuantity.subtract(receivedQuantity);
    }
}