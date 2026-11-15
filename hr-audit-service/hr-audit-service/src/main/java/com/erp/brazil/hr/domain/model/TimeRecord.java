package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_record",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_time_record_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_time_record_employee_date_type", columnNames = {"employee_id", "record_date", "record_type"})
    },
    indexes = {
        @Index(name = "idx_time_record_employee_date", columnList = "employee_id,record_date"),
        @Index(name = "idx_time_record_status", columnList = "status"),
        @Index(name = "idx_time_record_date_range", columnList = "record_date")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20)
    private TimeRecordType recordType;

    @NotNull
    @Column(name = "record_time", nullable = false)
    private LocalTime recordTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "record_source", nullable = false, length = 20)
    private TimeRecordSource recordSource;

    @Column(name = "latitude", precision = 10, scale = 8)
    private Double latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private Double longitude;

    @Size(max = 100)
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "is_manual_adjustment")
    private boolean isManualAdjustment = false;

    @Size(max = 500)
    @Column(name = "adjustment_reason", length = 500)
    private String adjustmentReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by")
    private Employee adjustedBy;

    @Column(name = "adjusted_at")
    private java.time.Instant adjustedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeRecordStatus status = TimeRecordStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(name = "approved_at")
    private java.time.Instant approvedAt;

    public boolean isEntry() {
        return recordType == TimeRecordType.ENTRY || recordType == TimeRecordType.RETURN_LUNCH || recordType == TimeRecordType.OVERTIME_ENTRY;
    }

    public boolean isExit() {
        return recordType == TimeRecordType.EXIT_LUNCH || recordType == TimeRecordType.EXIT || recordType == TimeRecordType.OVERTIME_EXIT;
    }

    public void setIsManualAdjustment(boolean isManualAdjustment) {
        this.isManualAdjustment = isManualAdjustment;
    }
}