package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * JPA Entity for ICMS tax rules (persistence).
 * Domain logic uses the ICMSRule record (sealed interface implementation).
 */
@Entity
@Table(name = "fiscal_icms_rule")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
public class ICMSEntity extends TaxRuleEntity {
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
        // Domain logic uses ICMSRule record, this is for JPA persistence only
        throw new UnsupportedOperationException("Use ICMSRule domain record for tax calculation");
    }
}