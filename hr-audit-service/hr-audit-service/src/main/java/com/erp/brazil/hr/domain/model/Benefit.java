package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "benefit",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_benefit_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_benefit_company_code", columnNames = {"company_id", "code"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benefit extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type", nullable = false, length = 30)
    private BenefitType benefitType;

    @Size(max = 255)
    @Column(name = "provider_name", length = 255)
    private String providerName;

    @Size(max = 18)
    @Column(name = "provider_cnpj", length = 18)
    private String providerCnpj;

    @Size(max = 100)
    @Column(name = "plan_name", length = 100)
    private String planName;

    @Size(max = 50)
    @Column(name = "plan_number", length = 50)
    private String planNumber;

    @Column(name = "employee_contribution_pct", precision = 5, scale = 2)
    private BigDecimal employeeContributionPct = BigDecimal.ZERO;

    @Column(name = "employer_contribution_pct", precision = 5, scale = 2)
    private BigDecimal employerContributionPct = BigDecimal.valueOf(100);

    @Column(name = "max_employee_value", precision = 10, scale = 2)
    private BigDecimal maxEmployeeValue;

    @Column(name = "max_employer_value", precision = 10, scale = 2)
    private BigDecimal maxEmployerValue;

    @Column(name = "is_mandatory")
    private boolean isMandatory = false;

    @Column(name = "is_taxable")
    private boolean isTaxable = false;

    @Column(name = "requires_dependent")
    private boolean requiresDependent = false;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    public boolean isActiveOn(LocalDate date) {
        return isActive && !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }

    public BigDecimal calculateEmployeeContribution(BigDecimal baseValue) {
        if (employeeContributionPct == null || employeeContributionPct.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal contribution = baseValue.multiply(employeeContributionPct).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        if (maxEmployeeValue != null && contribution.compareTo(maxEmployeeValue) > 0) {
            return maxEmployeeValue;
        }
        return contribution;
    }

    public BigDecimal calculateEmployerContribution(BigDecimal baseValue) {
        if (employerContributionPct == null || employerContributionPct.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal contribution = baseValue.multiply(employerContributionPct).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        if (maxEmployerValue != null && contribution.compareTo(maxEmployerValue) > 0) {
            return maxEmployerValue;
        }
        return contribution;
    }

    public enum BenefitType {
        TRANSPORT_VOUCHER, MEAL_VOUCHER, FOOD_VOUCHER, HEALTH_INSURANCE,
        DENTAL_INSURANCE, LIFE_INSURANCE, PENSION, GYM, EDUCATION,
        CHILDCARE, FUEL, PHARMACY, OPTICAL, OTHER
    }
}