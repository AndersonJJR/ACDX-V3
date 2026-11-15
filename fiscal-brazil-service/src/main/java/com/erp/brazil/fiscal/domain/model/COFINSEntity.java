package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA Entity for COFINS tax rules (persistence).
 * Domain logic uses the PISCOFINSRule record with isPis=false (sealed interface implementation).
 */
@Entity
@Table(name = "fiscal_cofins_rule")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class COFINSEntity extends TaxRuleEntity {
    @Column(name = "cst", length = 2)
    private String cst; // 01, 02, 03, 04, 05, 06, 07, 08, 09, 49, 50, 51, 52, 53, 54, 55, 56, 60, 61, 62, 63, 64, 65, 66, 67, 70, 71, 72, 73, 74, 75, 98, 99

    @Column(name = "aliquota", precision = 5, scale = 4)
    private BigDecimal aliquota;

    @Column(name = "valor_aliquota", precision = 15, scale = 4)
    private BigDecimal valorAliquota;

    @Column(name = "calculation_type", length = 20)
    private String calculationType; // PERCENTUAL, VALOR_FIXO

    @Override
    public BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context) {
        // Domain logic uses PISCOFINSRule record, this is for JPA persistence only
        throw new UnsupportedOperationException("Use PISCOFINSRule domain record for tax calculation");
    }
}