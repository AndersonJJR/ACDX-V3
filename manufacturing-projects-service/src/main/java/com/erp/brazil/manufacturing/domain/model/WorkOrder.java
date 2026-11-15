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
@Table(name = "work_order", indexes = {
    @Index(name = "idx_wo_number", columnList = "order_number", unique = true),
    @Index(name = "idx_wo_prod_order", columnList = "production_order_id"),
    @Index(name = "idx_wo_workcenter", columnList = "work_center_id"),
    @Index(name = "idx_wo_status", columnList = "status"),
    @Index(name = "idx_wo_dates", columnList = "planned_start_date,planned_end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_order_id", nullable = false)
    private ProductionOrder productionOrder;

    @Column(name = "production_order_operation_id")
    private UUID productionOrderOperationId;

    @Column(name = "work_center_id")
    private UUID workCenterId;

    @Column(name = "work_station_id")
    private UUID workStationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkOrderStatus status = WorkOrderStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority = Priority.NORMAL;

    @Column(name = "operation_name", length = 200)
    private String operationName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "planned_quantity", precision = 19, scale = 6)
    private BigDecimal plannedQuantity;

    @Column(name = "started_quantity", precision = 19, scale = 6)
    private BigDecimal startedQuantity = BigDecimal.ZERO;

    @Column(name = "completed_quantity", precision = 19, scale = 6)
    private BigDecimal completedQuantity = BigDecimal.ZERO;

    @Column(name = "scrapped_quantity", precision = 19, scale = 6)
    private BigDecimal scrappedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure;

    @Column(name = "planned_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal plannedSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "planned_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal plannedRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "actual_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal actualSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "actual_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal actualRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "planned_start_date")
    private LocalDateTime plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDateTime plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "notes", length = 2000)
    private String notes;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkOrderOperation> operations = new ArrayList<>();

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimeLog> timeLogs = new ArrayList<>();

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

    public enum WorkOrderStatus {
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

    public void addOperation(WorkOrderOperation operation) {
        operations.add(operation);
        operation.setWorkOrder(this);
    }

    public void removeOperation(WorkOrderOperation operation) {
        operations.remove(operation);
        operation.setWorkOrder(null);
    }

    public void addTimeLog(TimeLog timeLog) {
        timeLogs.add(timeLog);
        timeLog.setWorkOrder(this);
    }

    public void removeTimeLog(TimeLog timeLog) {
        timeLogs.remove(timeLog);
        timeLog.setWorkOrder(null);
    }

    public BigDecimal getRemainingQuantity() {
        return plannedQuantity.subtract(completedQuantity).subtract(scrappedQuantity);
    }
}