package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Benefit(
    Long id,
    UUID uuid,
    Long companyId,
    String code,
    String name,
    String description,
    BenefitType benefitType,
    String providerName,
    String providerCnpj,
    String planName,
    String planNumber,
    BigDecimal employeeContributionPct,
    BigDecimal employerContributionPct,
    BigDecimal maxEmployeeValue,
    BigDecimal maxEmployerValue,
    boolean isMandatory,
    boolean isTaxable,
    boolean requiresDependent,
    LocalDate startDate,
    LocalDate endDate,
    boolean isActive
) {
    public enum BenefitType {
        TRANSPORT_VOUCHER, MEAL_VOUCHER, FOOD_VOUCHER, HEALTH_INSURANCE,
        DENTAL_INSURANCE, LIFE_INSURANCE, PENSION, GYM, EDUCATION,
        CHILDCARE, FUEL, PHARMACY, OPTICAL, OTHER
    }

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
}