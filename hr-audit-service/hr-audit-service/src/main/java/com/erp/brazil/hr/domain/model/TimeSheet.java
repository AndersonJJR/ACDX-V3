package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "time_sheet",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_time_sheet_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_time_sheet_employee_period", columnNames = {"employee_id", "reference_month", "reference_year"})
    },
    indexes = {
        @Index(name = "idx_time_sheet_period", columnList = "reference_year,reference_month"),
        @Index(name = "idx_time_sheet_status", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSheet extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "reference_month", nullable = false)
    private Integer referenceMonth;

    @NotNull
    @Min(2000)
    @Max(2100)
    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_worked_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalWorkedHours = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_overtime_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalOvertimeHours = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_night_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalNightHours = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_absence_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalAbsenceHours = BigDecimal.ZERO;

    @Column(name = "total_late_minutes")
    private int totalLateMinutes = 0;

    @Column(name = "total_early_departure_minutes")
    private int totalEarlyDepartureMinutes = 0;

    @Column(name = "days_worked")
    private int daysWorked = 0;

    @Column(name = "days_absent")
    private int daysAbsent = 0;

    @Column(name = "days_holiday")
    private int daysHoliday = 0;

    @Column(name = "days_vacation")
    private int daysVacation = 0;

    @Column(name = "days_medical_leave")
    private int daysMedicalLeave = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeSheetStatus status = TimeSheetStatus.OPEN;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by")
    private Employee calculatedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by")
    private Employee closedBy;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    public LocalDate getPeriodStart() {
        return LocalDate.of(referenceYear, referenceMonth, 1);
    }

    public LocalDate getPeriodEnd() {
        return getPeriodStart().withDayOfMonth(getPeriodStart().lengthOfMonth());
    }

    public boolean isClosed() {
        return status == TimeSheetStatus.CLOSED;
    }

    public enum TimeSheetStatus {
        OPEN, CALCULATED, APPROVED, CLOSED, DISPUTED
    }
}