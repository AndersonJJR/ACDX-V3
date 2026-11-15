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
@Table(name = "work_station", indexes = {
    @Index(name = "idx_work_station_workcenter", columnList = "work_center_id"),
    @Index(name = "idx_work_station_code", columnList = "code", unique = true),
    @Index(name = "idx_work_station_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkStation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_center_id", nullable = false)
    private WorkCenter workCenter;

    @NotNull
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkStationStatus status = WorkStationStatus.ACTIVE;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "capacity_per_day_minutes", precision = 10, scale = 2)
    private BigDecimal capacityPerDayMinutes = BigDecimal.valueOf(480);

    @Column(name = "efficiency_percentage", precision = 5, scale = 2)
    private BigDecimal efficiencyPercentage = BigDecimal.valueOf(100);

    @Column(name = "hourly_rate", precision = 19, scale = 4)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "BRL";

    @Column(name = "location_x", precision = 10, scale = 2)
    private BigDecimal locationX;

    @Column(name = "location_y", precision = 10, scale = 2)
    private BigDecimal locationY;

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

    public enum WorkStationStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        BLOCKED
    }
}