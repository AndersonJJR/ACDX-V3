package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ISS (Imposto Sobre Serviços) calculation rule.
 */
public record ISSRule(
        String id,
        String taxType,
        String name,
        String description,
        String ufOrigin,
        String ufDestination,
        String ncmCode,
        String cestCode,
        String cfopCode,
        String taxRegime,
        java.time.LocalDate effectiveFrom,
        java.time.LocalDate effectiveTo,
        int priority,
        boolean active,
        String version,
        java.time.LocalDateTime lastSync,
        String municipalityCode,
        String serviceCode,
        String cst,
        BigDecimal aliquota,
        BigDecimal deduction,
        boolean isExempt
) implements TaxRule {

    @Override
    public BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context) {
        if (baseValue == null || isExempt) {
            return BigDecimal.ZERO;
        }

        BigDecimal bc = baseValue;
        if (deduction != null && deduction.compareTo(BigDecimal.ZERO) > 0) {
            bc = bc.subtract(deduction).max(BigDecimal.ZERO);
        }

        return bc.multiply(aliquota != null ? aliquota : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    public static ISSRule of(String municipalityCode, String serviceCode, String cst,
                              BigDecimal aliquota, BigDecimal deduction, boolean isExempt) {
        return new ISSRule(
                java.util.UUID.randomUUID().toString(),
                "ISS",
                "ISS - " + municipalityCode + " - " + serviceCode,
                "ISS rule for municipality " + municipalityCode + " service " + serviceCode,
                null, null, null, null, null, null,
                java.time.LocalDate.now(), null, 0, true, "1.0",
                java.time.LocalDateTime.now(),
                municipalityCode, serviceCode, cst, aliquota, deduction, isExempt
        );
    }
}