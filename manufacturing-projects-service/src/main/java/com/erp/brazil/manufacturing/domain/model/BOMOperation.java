package com.erp.brazil.manufacturing.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bom_operation", indexes = {
    @Index(name = "idx_bom_operation_bom", columnList = "bom_id"),
    @Index(name = "idx_bom_operation_workcenter", columnList = "work_center_id"),
    @Index(name = "idx_bom_operation_sequence", columnList = "sequence")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BOMOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BOM bom;

    @NotNull
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

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
}