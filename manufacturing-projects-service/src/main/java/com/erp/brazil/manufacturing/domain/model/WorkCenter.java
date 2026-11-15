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
@Table(name = "work_center", indexes = {
    @Index(name = "idx_work_center_code", columnList = "code", unique = true),
    @Index(name = "idx_work_center_type", columnList = "type"),
    @Index(name = "idx_work_center_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private WorkCenterType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkCenterStatus status = WorkCenterStatus.ACTIVE;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "cost_center_id")
    private UUID costCenterId;

    @Column(name = "capacity_per_day_minutes", precision = 10, scale = 2)
    private BigDecimal capacityPerDayMinutes = BigDecimal.valueOf(480); // 8 hours

    @Column(name = "efficiency_percentage", precision = 5, scale = 2)
    private BigDecimal efficiencyPercentage = BigDecimal.valueOf(100);

    @Column(name = "utilization_percentage", precision = 5, scale = 2)
    private BigDecimal utilizationPercentage = BigDecimal.valueOf(85);

    @Column(name = "setup_time_minutes", precision = 10, scale = 2)
    private BigDecimal setupTimeMinutes = BigDecimal.ZERO;

    @Column(name = "hourly_rate", precision = 19, scale = 4)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "shift_pattern_id")
    private UUID shiftPatternId;

    @Column(name = "calendar_id")
    private UUID calendarId;

    @OneToMany(mappedBy = "workCenter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkStation> workStations = new ArrayList<>();

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

    public enum WorkCenterType {
        MACHINE,
        LABOR,
        ASSEMBLY,
        INSPECTION,
        PACKAGING,
        WAREHOUSE,
        SUBCONTRACTING,
        OTHER
    }

    public enum WorkCenterStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        DECOMMISSIONED
    }

    public void addWorkStation(WorkStation workStation) {
        workStations.add(workStation);
        workStation.setWorkCenter(this);
    }

    public void removeWorkStation(WorkStation workStation) {
        workStations.remove(workStation);
        workStation.setWorkCenter(null);
    }
}