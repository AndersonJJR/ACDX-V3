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
@Table(name = "production_order", indexes = {
    @Index(name = "idx_prod_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_prod_order_product", columnList = "product_id"),
    @Index(name = "idx_prod_order_status", columnList = "status"),
    @Index(name = "idx_prod_order_type", columnList = "order_type"),
    @Index(name = "idx_prod_order_dates", columnList = "planned_start_date,planned_end_date"),
    @Index(name = "idx_prod_order_sales_order", columnList = "sales_order_id"),
    @Index(name = "idx_prod_order_project", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductionOrderStatus status = ProductionOrderStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority = Priority.NORMAL;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull
    @Column(name = "ordered_quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal orderedQuantity;

    @Column(name = "produced_quantity", precision = 19, scale = 6)
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @Column(name = "scrapped_quantity", precision = 19, scale = 6)
    private BigDecimal scrappedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "bom_id")
    private UUID bomId;

    @Column(name = "routing_id")
    private UUID routingId;

    @Column(name = "sales_order_id")
    private UUID salesOrderId;

    @Column(name = "sales_order_item_id")
    private UUID salesOrderItemId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(name = "released_date")
    private LocalDateTime releasedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "total_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal totalSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "total_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal totalRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 19, scale = 4)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductionOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductionOrderOperation> operations = new ArrayList<>();

    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkOrder> workOrders = new ArrayList<>();

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

    public enum OrderType {
        MTO,           // Make to Order
        MTS,           // Make to Stock
        MTS_MTO,       // Make to Stock / Make to Order
        ENGINEERING,
        REWORK,
        PROTOTYPE,
        SERVICE
    }

    public enum ProductionOrderStatus {
        PLANNED,
        RELEASED,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        CANCELLED,
        CLOSED
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT,
        CRITICAL
    }

    public void addItem(ProductionOrderItem item) {
        items.add(item);
        item.setProductionOrder(this);
    }

    public void removeItem(ProductionOrderItem item) {
        items.remove(item);
        item.setProductionOrder(null);
    }

    public void addOperation(ProductionOrderOperation operation) {
        operations.add(operation);
        operation.setProductionOrder(this);
    }

    public void removeOperation(ProductionOrderOperation operation) {
        operations.remove(operation);
        operation.setProductionOrder(null);
    }

    public void addWorkOrder(WorkOrder workOrder) {
        workOrders.add(workOrder);
        workOrder.setProductionOrder(this);
    }

    public void removeWorkOrder(WorkOrder workOrder) {
        workOrders.remove(workOrder);
        workOrder.setProductionOrder(null);
    }

    public BigDecimal getRemainingQuantity() {
        return orderedQuantity.subtract(producedQuantity).subtract(scrappedQuantity);
    }

    public boolean isCompleted() {
        return status == ProductionOrderStatus.COMPLETED || status == ProductionOrderStatus.CLOSED;
    }

    public boolean canBeReleased() {
        return status == ProductionOrderStatus.PLANNED;
    }

    public boolean canBeStarted() {
        return status == ProductionOrderStatus.RELEASED;
    }
}