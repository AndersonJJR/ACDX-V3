package com.erp.brazil.fiscal.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ICMS (Imposto sobre Circulação de Mercadorias e Serviços) calculation rule.
 */
public record ICMSRule(
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
        String modalidadeBc,
        String modalidadeBcSt,
        String origem,
        String cst,
        String csosn,
        BigDecimal aliquota,
        BigDecimal aliquotaSt,
        BigDecimal aliquotaFcp,
        BigDecimal aliquotaFcpSt,
        BigDecimal reducaoBc,
        BigDecimal reducaoBcSt,
        BigDecimal mvaAjustado,
        BigDecimal pautaFiscal,
        boolean st,
        boolean fcp
) implements TaxRule {

    @Override
    public BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context) {
        if (baseValue == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal bc = baseValue;
        if (reducaoBc != null && reducaoBc.compareTo(BigDecimal.ZERO) > 0) {
            bc = bc.multiply(BigDecimal.ONE.subtract(reducaoBc.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        }

        BigDecimal result = bc.multiply(aliquota != null ? aliquota : BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // ST calculation if applicable
        if (st && aliquotaSt != null) {
            BigDecimal bcSt = baseValue;
            if (mvaAjustado != null && mvaAjustado.compareTo(BigDecimal.ZERO) > 0) {
                bcSt = bcSt.multiply(BigDecimal.ONE.add(mvaAjustado.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
            }
            if (reducaoBcSt != null && reducaoBcSt.compareTo(BigDecimal.ZERO) > 0) {
                bcSt = bcSt.multiply(BigDecimal.ONE.subtract(reducaoBcSt.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
            }

            BigDecimal stValue = bcSt.multiply(aliquotaSt)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .subtract(result);

            if (stValue.compareTo(BigDecimal.ZERO) > 0) {
                result = result.add(stValue);
            }
        }

        // FCP calculation
        if (fcp && aliquotaFcp != null) {
            BigDecimal fcpValue = bc.multiply(aliquotaFcp)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            result = result.add(fcpValue);
        }

        return result.max(BigDecimal.ZERO);
    }

    public static ICMSRule of(String modalidadeBc, String modalidadeBcSt, String origem,
                               String cst, String csosn, BigDecimal aliquota,
                               BigDecimal aliquotaSt, BigDecimal aliquotaFcp,
                               BigDecimal aliquotaFcpSt, BigDecimal reducaoBc,
                               BigDecimal reducaoBcSt, BigDecimal mvaAjustado,
                               BigDecimal pautaFiscal, boolean st, boolean fcp) {
        return new ICMSRule(
                java.util.UUID.randomUUID().toString(),
                "ICMS",
                "ICMS - " + modalidadeBc,
                "ICMS rule with modalidade BC " + modalidadeBc,
                null, null, null, null, null, null,
                java.time.LocalDate.now(), null, 0, true, "1.0",
                java.time.LocalDateTime.now(),
                modalidadeBc, modalidadeBcSt, origem, cst, csosn,
                aliquota, aliquotaSt, aliquotaFcp, aliquotaFcpSt,
                reducaoBc, reducaoBcSt, mvaAjustado, pautaFiscal, st, fcp
        );
    }
}