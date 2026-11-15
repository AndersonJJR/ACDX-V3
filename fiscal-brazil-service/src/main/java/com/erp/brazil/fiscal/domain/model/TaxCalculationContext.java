package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationContext {
    private String companyId;
    private String companyCnpj;
    private String companyState;
    private String companyMunicipality;
    private TaxRegime taxRegime;

    private String partnerId;
    private String partnerCnpj;
    private String partnerState;
    private String partnerMunicipality;
    private boolean partnerIsConsumerFinal;
    private boolean partnerIsForeign;

    private String operationType; // VENDA, COMPRA, TRANSFERENCIA, DEVOLUCAO, SERVICO
    private String cfopCode;
    private String ncmCode;
    private String cestCode;
    private String cnaeCode;
    private String serviceCode;

    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal totalValue;
    private BigDecimal discountValue;
    private BigDecimal freightValue;
    private BigDecimal insuranceValue;
    private BigDecimal otherExpensesValue;

    private String nfeModel; // 55 (NF-e), 65 (NFC-e), 21/22 (NFS-e)
    private String environment; // 1-Produção, 2-Homologação

    private Map<String, Object> additionalData;
    private List<String> appliedRules;
}