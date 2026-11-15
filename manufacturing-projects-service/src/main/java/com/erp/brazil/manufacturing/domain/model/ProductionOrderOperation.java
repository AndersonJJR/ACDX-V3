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
@Table(name = "production_order_operation", indexes = {
    @Index(name = "idx_po_op_prod_order", columnList = "production_order_id"),
    @Index(name = "idx_po_op_workcenter", columnList = "work_center_id"),
    @Index(name = "idx_po_op_status", columnList = "status"),
    @Index(name = "idx_po_op_sequence", columnList = "sequence")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @NotNull
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "routing_operation_id")
    private UUID routingOperationId;

    @Column(name = "work_center_id")
    private UUID workCenterId;

    @Column(name = "work_station_id")
    private UUID workStationId;

    @Column(name = "operation_name", length = 200)
    private String operationName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "planned_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal plannedSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "planned_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal plannedRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "actual_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal actualSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "actual_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal actualRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "crew_size")
    private Integer crewSize = 1;

    @Column(name = "is_outsourced")
    private Boolean isOutsourced = false;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "subcontracting_order_id")
    private UUID subcontractingOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OperationStatus status = OperationStatus.PENDING;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(name = "completed_quantity", precision = 19, scale = 6)
    private BigDecimal completedQuantity = BigDecimal.ZERO;

    @Column(name = "scrapped_quantity", precision = 19, scale = 6)
    private BigDecimal scrappedQuantity = BigDecimal.ZERO;

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

    public enum OperationStatus {
        PENDING,
        WAITING_MATERIAL,
        WAITING_CAPACITY,
        READY,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        SKIPPED,
        CANCELLED
    }

    public BigDecimal getTotalPlannedTimeMinutes() {
        return plannedSetupTimeMinutes.add(plannedRunTimeMinutes);
    }

    public BigDecimal getTotalActualTimeMinutes() {
        return actualSetupTimeMinutes.add(actualRunTimeMinutes);
    }

    public boolean isCompleted() {
        return status == OperationStatus.COMPLETED || status == OperationStatus.SKIPPED;
    }
}