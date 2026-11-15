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
@Table(name = "fiscal_ipi_rule")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class IPIRule extends TaxRule {
    @Column(name = "cst", length = 2)
    private String cst; // 00, 01, 02, 03, 04, 05, 49, 50, 51, 52, 53, 54, 55, 99

    @Column(name = "aliquota", precision = 5, scale = 2)
    private BigDecimal aliquota;

    @Column(name = "valor_fixo", precision = 15, scale = 4)
    private BigDecimal valorFixo;

    @Column(name = "unidade", length = 10)
    private String unidade;

    @Column(name = "calculation_type", length = 20)
    private String calculationType; // PERCENTUAL, VALOR_FIXO

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
}