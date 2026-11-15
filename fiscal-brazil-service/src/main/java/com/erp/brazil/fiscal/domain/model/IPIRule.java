package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * IPI (Imposto sobre Produtos Industrializados) calculation rule.
 */
public record IPIRule(
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
        BigDecimal valorFixo,
        String unidade,
        String calculationType
) implements TaxRule {

    @Override
    public BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context) {
        if (baseValue == null) {
            return BigDecimal.ZERO;
        }

        if ("VALOR_FIXO".equals(calculationType) && valorFixo != null) {
            BigDecimal qty = context.getQuantity() != null ? context.getQuantity() : BigDecimal.ONE;
            return valorFixo.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        }

        return baseValue.multiply(aliquota != null ? aliquota : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    public static IPIRule of(String cst, BigDecimal aliquota, BigDecimal valorFixo,
                              String unidade, String calculationType) {
        return new IPIRule(
                java.util.UUID.randomUUID().toString(),
                "IPI",
                "IPI - CST " + cst,
                "IPI rule for CST " + cst,
                null, null, null, null, null, null,
                java.time.LocalDate.now(), null, 0, true, "1.0",
                java.time.LocalDateTime.now(),
                cst, aliquota, valorFixo, unidade, calculationType
        );
    }
}