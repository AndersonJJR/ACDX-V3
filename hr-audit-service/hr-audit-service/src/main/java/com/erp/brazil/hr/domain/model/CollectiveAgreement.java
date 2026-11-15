package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "collective_agreement",
    uniqueConstraints = @UniqueConstraint(name = "uk_collective_agreement_union_code", columnNames = {"union_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectiveAgreement extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "union_id", nullable = false)
    private Union union;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "base_salary_minimum", precision = 12, scale = 2)
    private BigDecimal baseSalaryMinimum;

    @Column(name = "work_journey_hours", precision = 4, scale = 2)
    private BigDecimal workJourneyHours;

    @Column(name = "overtime_rate_daytime", precision = 4, scale = 2)
    private BigDecimal overtimeRateDaytime = BigDecimal.valueOf(50.00);

    @Column(name = "overtime_rate_nighttime", precision = 4, scale = 2)
    private BigDecimal overtimeRateNighttime = BigDecimal.valueOf(75.00);

    @Column(name = "overtime_rate_holiday", precision = 4, scale = 2)
    private BigDecimal overtimeRateHoliday = BigDecimal.valueOf(100.00);

    @Column(name = "night_shift_additional", precision = 4, scale = 2)
    private BigDecimal nightShiftAdditional = BigDecimal.valueOf(20.00);

    @Column(name = "hazardous_additional", precision = 4, scale = 2)
    private BigDecimal hazardousAdditional = BigDecimal.valueOf(30.00);

    @Column(name = "unhealthy_additional", precision = 4, scale = 2)
    private BigDecimal unhealthyAdditional = BigDecimal.valueOf(40.00);

    @Size(max = 500)
    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public boolean isActiveOn(LocalDate date) {
        return isActive &&
               !startDate.isAfter(date) &&
               (endDate == null || !endDate.isBefore(date));
    }
}