package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PIS/COFINS calculation rule (unified for both taxes with different CSTs).
 */
public record PISCOFINSRule(
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
        String cst,
        BigDecimal aliquota,
        BigDecimal valorAliquota,
        String calculationType, // PERCENTUAL, VALOR_FIXO
        boolean isPis
) implements TaxRule {

    @Override
    public BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context) {
        if (baseValue == null) {
            return BigDecimal.ZERO;
        }

        if ("VALOR_FIXO".equals(calculationType) && valorAliquota != null) {
            BigDecimal qty = context.getQuantity() != null ? context.getQuantity() : BigDecimal.ONE;
            return valorAliquota.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        }

        return baseValue.multiply(aliquota != null ? aliquota : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    public static PISCOFINSRule pis(String cst, BigDecimal aliquota, BigDecimal valorAliquota,
                                     String calculationType) {
        return new PISCOFINSRule(
                java.util.UUID.randomUUID().toString(),
                "PIS",
                "PIS - CST " + cst,
                "PIS rule for CST " + cst,
                null, null, null, null, null, null,
                java.time.LocalDate.now(), null, 0, true, "1.0",
                java.time.LocalDateTime.now(),
                cst, aliquota, valorAliquota, calculationType, true
        );
    }

    public static PISCOFINSRule cofins(String cst, BigDecimal aliquota, BigDecimal valorAliquota,
                                        String calculationType) {
        return new PISCOFINSRule(
                java.util.UUID.randomUUID().toString(),
                "COFINS",
                "COFINS - CST " + cst,
                "COFINS rule for CST " + cst,
                null, null, null, null, null, null,
                java.time.LocalDate.now(), null, 0, true, "1.0",
                java.time.LocalDateTime.now(),
                cst, aliquota, valorAliquota, calculationType, false
        );
    }
}