package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "benefit",
    uniqueConstraints = @UniqueConstraint(name = "uk_benefit_company_code", columnNames = {"company_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Benefit extends BaseEntity {

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

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
    private Boolean isMandatory = false;

    @Column(name = "is_taxable")
    private Boolean isTaxable = false;

    @Column(name = "requires_dependent")
    private Boolean requiresDependent = false;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public boolean isActiveOn(LocalDate date) {
        return isActive &&
               !startDate.isAfter(date) &&
               (endDate == null || !endDate.isBefore(date));
    }

    public BigDecimal calculateEmployeeValue(BigDecimal baseValue) {
        if (baseValue == null) return BigDecimal.ZERO;
        BigDecimal value = baseValue.multiply(employeeContributionPct).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        if (maxEmployeeValue != null && value.compareTo(maxEmployeeValue) > 0) {
            return maxEmployeeValue;
        }
        return value;
    }

    public BigDecimal calculateEmployerValue(BigDecimal baseValue) {
        if (baseValue == null) return BigDecimal.ZERO;
        BigDecimal value = baseValue.multiply(employerContributionPct).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        if (maxEmployerValue != null && value.compareTo(maxEmployerValue) > 0) {
            return maxEmployerValue;
        }
        return value;
    }
}