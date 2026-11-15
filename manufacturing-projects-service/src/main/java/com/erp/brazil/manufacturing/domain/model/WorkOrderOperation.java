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
@Table(name = "work_order_operation", indexes = {
    @Index(name = "idx_wo_op_work_order", columnList = "work_order_id"),
    @Index(name = "idx_wo_op_sequence", columnList = "sequence"),
    @Index(name = "idx_wo_op_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @NotNull
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "operation_name", length = 200)
    private String operationName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "work_center_id")
    private UUID workCenterId;

    @Column(name = "work_station_id")
    private UUID workStationId;

    @Column(name = "planned_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal plannedSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "planned_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal plannedRunTimeMinutes = BigDecimal.ZERO;

    @Column(name = "actual_setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal actualSetupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "actual_run_time_minutes", precision = 10, scale = 2)
    private BigDecimal actualRunTimeMinutes = BigDecimal.ZERO;

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

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

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
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        SKIPPED,
        CANCELLED
    }
}