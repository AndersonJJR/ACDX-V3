package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA Entity for ISS tax rules (persistence).
 * Domain logic uses the ISSRule record (sealed interface implementation).
 */
@Entity
@Table(name = "fiscal_iss_rule")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class ISSEntity extends TaxRuleEntity {
    @Column(name = "municipality_code", length = 7)
    private String municipalityCode;

    @Column(name = "service_code", length = 10)
    private String serviceCode;

    @Column(name = "cst", length = 2)
    private String cst;

    @Column(name = "aliquota", precision = 5, scale = 2)
    private BigDecimal aliquota;

    @Column(name = "deduction", precision = 15, scale = 2)
    private BigDecimal deduction;

    @Column(name = "is_exempt")
    private boolean exempt = false;

    @Override
    public BigDecimal calculate(BigDecimal baseValue, TaxCalculationContext context) {
        // Domain logic uses ISSRule record, this is for JPA persistence only
        throw new UnsupportedOperationException("Use ISSRule domain record for tax calculation");
    }
}