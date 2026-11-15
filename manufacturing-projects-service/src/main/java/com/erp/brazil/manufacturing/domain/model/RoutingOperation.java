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
@Table(name = "routing_operation", indexes = {
    @Index(name = "idx_routing_op_routing", columnList = "routing_id"),
    @Index(name = "idx_routing_op_workcenter", columnList = "work_center_id"),
    @Index(name = "idx_routing_op_sequence", columnList = "sequence")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routing_id", nullable = false)
    private Routing routing;

    @NotNull
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @Column(name = "operation_number", length = 50)
    private String operationNumber;

    @Column(name = "work_center_id")
    private UUID workCenterId;

    @Column(name = "work_station_id")
    private UUID workStationId;

    @Column(name = "operation_name", length = 200)
    private String operationName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal setupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "run_time_minutes", precision = 10, scale = 2)
    private BigDecimal runTimeMinutes = BigDecimal.ZERO;

    @Column(name = "queue_time_minutes", precision = 10, scale = 2)
    private BigDecimal queueTimeMinutes = BigDecimal.ZERO;

    @Column(name = "move_time_minutes", precision = 10, scale = 2)
    private BigDecimal moveTimeMinutes = BigDecimal.ZERO;

    @Column(name = "crew_size")
    private Integer crewSize = 1;

    @Column(name = "is_outsourced")
    private Boolean isOutsourced = false;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Column(name = "inspection_required")
    private Boolean inspectionRequired = false;

    @Column(name = "sampling_plan_id")
    private UUID samplingPlanId;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "next_operation_id")
    private UUID nextOperationId;

    @Column(name = "previous_operation_id")
    private UUID previousOperationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long versionLock;
}