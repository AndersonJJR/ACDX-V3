package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "time_sheet",
    uniqueConstraints = @UniqueConstraint(name = "uk_time_sheet_employee_period", columnNames = {"employee_id", "reference_month", "reference_year"}),
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
    @Column(name = "total_worked_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalWorkedHours = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_overtime_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalOvertimeHours = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_night_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalNightHours = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_absence_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal totalAbsenceHours = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_late_minutes", nullable = false)
    private Integer totalLateMinutes = 0;

    @NotNull
    @Column(name = "total_early_departure_minutes", nullable = false)
    private Integer totalEarlyDepartureMinutes = 0;

    @NotNull
    @Column(name = "days_worked", nullable = false)
    private Integer daysWorked = 0;

    @NotNull
    @Column(name = "days_absent", nullable = false)
    private Integer daysAbsent = 0;

    @NotNull
    @Column(name = "days_holiday", nullable = false)
    private Integer daysHoliday = 0;

    @NotNull
    @Column(name = "days_vacation", nullable = false)
    private Integer daysVacation = 0;

    @NotNull
    @Column(name = "days_medical_leave", nullable = false)
    private Integer daysMedicalLeave = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeSheetStatus status = TimeSheetStatus.OPEN;

    @Column(name = "calculated_at")
    private java.time.Instant calculatedAt;

    @Column(name = "calculated_by")
    private Long calculatedBy;

    @Column(name = "approved_at")
    private java.time.Instant approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "closed_at")
    private java.time.Instant closedAt;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    public YearMonth getReferencePeriod() {
        return YearMonth.of(referenceYear, referenceMonth);
    }

    public boolean isEditable() {
        return status.isEditable();
    }

    public boolean isFinal() {
        return status.isFinal();
    }

    public int getTotalDays() {
        return daysWorked + daysAbsent + daysHoliday + daysVacation + daysMedicalLeave;
    }

    public BigDecimal getTotalHours() {
        return totalWorkedHours.add(totalOvertimeHours).add(totalNightHours);
    }
}