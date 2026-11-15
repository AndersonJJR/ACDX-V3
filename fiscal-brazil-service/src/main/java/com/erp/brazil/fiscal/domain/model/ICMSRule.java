package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "fiscal_icms_rule")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class ICMSRule extends TaxRule {
    @Column(name = "modalidade_bc", length = 2)
    private String modalidadeBc; // 00, 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13, 14, 15, 16, 17, 18

    @Column(name = "modalidade_bc_st", length = 2)
    private String modalidadeBcSt; // 00, 01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12, 13, 14, 15, 16

    @Column(name = "origem", length = 1)
    private String origem; // 0-8

    @Column(name = "cst", length = 3)
    private String cst; // 00, 10, 20, 30, 40, 41, 50, 51, 60, 70, 90

    @Column(name = "csosn", length = 3)
    private String csosn; // 101, 102, 103, 201, 202, 203, 300, 400, 500, 900

    @Column(name = "aliquota", precision = 5, scale = 2)
    private BigDecimal aliquota;

    @Column(name = "aliquota_st", precision = 5, scale = 2)
    private BigDecimal aliquotaSt;

    @Column(name = "aliquota_fcp", precision = 5, scale = 2)
    private BigDecimal aliquotaFcp;

    @Column(name = "aliquota_fcp_st", precision = 5, scale = 2)
    private BigDecimal aliquotaFcpSt;

    @Column(name = "reducao_bc", precision = 5, scale = 2)
    private BigDecimal reducaoBc;

    @Column(name = "reducao_bc_st", precision = 5, scale = 2)
    private BigDecimal reducaoBcSt;

    @Column(name = "mva_ajustado", precision = 5, scale = 2)
    private BigDecimal mvaAjustado;

    @Column(name = "pauta_fiscal", precision = 15, scale = 2)
    private BigDecimal pautaFiscal;

    @Column(name = "is_st")
    private boolean st = false;

    @Column(name = "is_fcp")
    private boolean fcp = false;

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
}