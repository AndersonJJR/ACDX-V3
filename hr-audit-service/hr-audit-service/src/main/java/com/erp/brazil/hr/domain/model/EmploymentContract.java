package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employment_contract",
    uniqueConstraints = @UniqueConstraint(name = "uk_contract_uuid", columnNames = "uuid"),
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
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
    @DecimalMin("0.01")
    @Column(name = "weekly_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal weeklyHours = new BigDecimal("44.00");

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "daily_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal dailyHours = new BigDecimal("8.00");

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "salary_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryBase;

    @Column(name = "salary_hourly", precision = 10, scale = 4, insertable = false, updatable = false)
    private BigDecimal salaryHourly;

    @NotBlank
    @Size(max = 150)
    @Column(name = "position_title", nullable = false, length = 150)
    private String positionTitle;

    @Size(max = 10)
    @Column(name = "position_cbo", length = 10)
    private String positionCbo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_to_id")
    private Employee reportingTo;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "union_id")
    private Union union;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collective_agreement_id")
    private CollectiveAgreement collectiveAgreement;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @NotNull
    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "renewal_count")
    private int renewalCount = 0;

    @Column(name = "fgts_opt_in")
    private boolean fgtsOptIn = true;

    @Size(max = 30)
    @Column(name = "fgts_account", length = 30)
    private String fgtsAccount;

    @Column(name = "benefit_transport_voucher")
    private boolean benefitTransportVoucher = true;

    @Column(name = "benefit_meal_voucher")
    private boolean benefitMealVoucher = true;

    @Column(name = "benefit_health_insurance")
    private boolean benefitHealthInsurance = false;

    @Column(name = "benefit_dental_insurance")
    private boolean benefitDentalInsurance = false;

    @Column(name = "benefit_life_insurance")
    private boolean benefitLifeInsurance = false;

    public boolean isActive(LocalDate referenceDate) {
        return !referenceDate.isBefore(contractStartDate) &&
               (contractEndDate == null || !referenceDate.isAfter(contractEndDate));
    }

    public BigDecimal calculateHourlyRate() {
        if (weeklyHours == null || weeklyHours.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return salaryBase.divide(
            weeklyHours.multiply(new BigDecimal("52")).divide(new BigDecimal("12")).divide(new BigDecimal("220"), 4, BigDecimal.ROUND_HALF_UP),
            4, BigDecimal.ROUND_HALF_UP
        );
    }

    public enum ContractType {
        CLT_INDEFINITE("CLT Indeterminado"),
        CLT_FIXED_TERM("CLT Determinado"),
        CLT_TEMPORARY("CLT Temporário"),
        APPRENTICE("Aprendiz"),
        INTERNSHIP("Estágio"),
        STATUTORY("Estatutário"),
        COOPERATIVE("Cooperado");

        private final String description;
        ContractType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum WorkJourney {
        FULL_TIME("Integral"),
        PART_TIME_MORNING("Meio Período Manhã"),
        PART_TIME_AFTERNOON("Meio Período Tarde"),
        PART_TIME_NIGHT("Meio Período Noite"),
        SHIFT_12X36("Escala 12x36"),
        SHIFT_6X1("Escala 6x1"),
        CUSTOM("Personalizada");

        private final String description;
        WorkJourney(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum WorkLocation {
        ONSITE("Presencial"),
        REMOTE("Remoto"),
        HYBRID("Híbrido");

        private final String description;
        WorkLocation(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}