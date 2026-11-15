package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employment_contract",
    uniqueConstraints = @UniqueConstraint(name = "uk_employment_contract_uuid", columnNames = "uuid"),
    indexes = {
        @Index(name = "idx_contract_employee", columnList = "employee_id"),
        @Index(name = "idx_contract_dates", columnList = "contract_start_date,contract_end_date")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploymentContract extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType contractType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "work_journey", nullable = false, length = 30)
    private WorkJourney workJourney;

    @NotNull
    @DecimalMin(value = "0.01", message = "Weekly hours must be positive")
    @Column(name = "weekly_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal weeklyHours = BigDecimal.valueOf(44.00);

    @NotNull
    @DecimalMin(value = "0.01", message = "Daily hours must be positive")
    @Column(name = "daily_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal dailyHours = BigDecimal.valueOf(8.00);

    @NotNull
    @Positive
    @Column(name = "salary_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryBase;

    @Column(name = "salary_hourly", precision = 10, scale = 4, insertable = false, updatable = false)
    private BigDecimal salaryHourly;

    @NotBlank
    @Column(name = "position_title", nullable = false, length = 150)
    private String positionTitle;

    @Size(max = 10)
    @Column(name = "position_cbo", length = 10)
    private String positionCbo;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "cost_center_id")
    private Long costCenterId;

    @Column(name = "reporting_to_id")
    private Long reportingToId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "work_location", nullable = false, length = 10)
    private WorkLocation workLocation = WorkLocation.ONSITE;

    @Size(max = 100)
    @Column(name = "work_city", length = 100)
    private String workCity;

    @Size(max = 2)
    @Column(name = "work_state", length = 2)
    private String workState;

    @Column(name = "union_id")
    private Long unionId;

    @Column(name = "collective_agreement_id")
    private Long collectiveAgreementId;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @NotNull
    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "renewal_count")
    private Integer renewalCount = 0;

    @Column(name = "fgts_opt_in")
    private Boolean fgtsOptIn = true;

    @Size(max = 30)
    @Column(name = "fgts_account", length = 30)
    private String fgtsAccount;

    @Column(name = "benefit_transport_voucher")
    private Boolean benefitTransportVoucher = true;

    @Column(name = "benefit_meal_voucher")
    private Boolean benefitMealVoucher = true;

    @Column(name = "benefit_health_insurance")
    private Boolean benefitHealthInsurance = false;

    @Column(name = "benefit_dental_insurance")
    private Boolean benefitDentalInsurance = false;

    @Column(name = "benefit_life_insurance")
    private Boolean benefitLifeInsurance = false;

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return contractStartDate != null &&
               !contractStartDate.isAfter(today) &&
               (contractEndDate == null || !contractEndDate.isBefore(today));
    }

    public boolean isOnProbation() {
        return probationEndDate != null && !probationEndDate.isBefore(LocalDate.now());
    }

    public boolean hasFgts() {
        return fgtsOptIn && contractType.hasFgts();
    }

    public boolean hasNoticePeriod() {
        return contractType.hasNoticePeriod();
    }

    public int getNoticePeriodDays() {
        if (!hasNoticePeriod()) return 0;
        int tenureYears = java.time.Period.between(contractStartDate, LocalDate.now()).getYears();
        if (tenureYears <= 1) return 30;
        return 30 + (tenureYears - 1) * 3; // 3 days per year after first, max 90
    }

    public BigDecimal calculateHourlyRate() {
        if (salaryBase == null || weeklyHours == null || weeklyHours.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        // Monthly hours = weekly_hours * 52 / 12 = weekly_hours * 4.333
        // Or standard 220 hours for 44h/week
        BigDecimal monthlyHours = weeklyHours.multiply(BigDecimal.valueOf(52))
            .divide(BigDecimal.valueOf(12), 4, BigDecimal.ROUND_HALF_UP);
        return salaryBase.divide(monthlyHours, 4, BigDecimal.ROUND_HALF_UP);
    }
}