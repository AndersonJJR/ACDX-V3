package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@Entity
@Table(name = "time_record",
    uniqueConstraints = @UniqueConstraint(name = "uk_time_record_employee_date_type", columnNames = {"employee_id", "record_date", "record_type"}),
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
    @Column(name = "record_type", nullable = false, length = 30)
    private TimeRecordType recordType;

    @NotNull
    @Column(name = "record_time", nullable = false)
    private LocalTime recordTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "record_source", nullable = false, length = 30)
    private TimeRecordSource recordSource;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Size(max = 100)
    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "is_manual_adjustment")
    private Boolean isManualAdjustment = false;

    @Size(max = 500)
    @Column(name = "adjustment_reason", length = 500)
    private String adjustmentReason;

    @Column(name = "adjusted_by")
    private Long adjustedBy;

    @Column(name = "adjusted_at")
    private java.time.Instant adjustedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeRecordStatus status = TimeRecordStatus.PENDING;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private java.time.Instant approvedAt;

    public boolean isWorkRecord() {
        return recordType.isWorkRecord();
    }

    public boolean isOvertimeRecord() {
        return recordType.isOvertimeRecord();
    }

    public boolean isAbsenceRecord() {
        return recordType.isAbsenceRecord();
    }

    public boolean isAutomatic() {
        return recordSource.isAutomatic();
    }

    public boolean isPending() {
        return status == TimeRecordStatus.PENDING;
    }

    public boolean isApproved() {
        return status == TimeRecordStatus.APPROVED;
    }
}