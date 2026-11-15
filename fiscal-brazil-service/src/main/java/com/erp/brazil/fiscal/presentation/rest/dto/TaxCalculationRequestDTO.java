package com.erp.brazil.fiscal.presentation.rest.dto;

import com.erp.brazil.fiscal.domain.model.TaxRegime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationRequestDTO {
    @NotBlank
    private String companyCnpj;

    private String companyState;

    private String companyMunicipality;

    @NotNull
    private TaxRegime taxRegime;

    private String partnerCnpj;

    private String partnerState;

    private String partnerMunicipality;

    private boolean partnerIsConsumerFinal = false;

    private boolean partnerIsForeign = false;

    @NotBlank
    private String operationType; // VENDA, COMPRA, TRANSFERENCIA, DEVOLUCAO, SERVICO

    private String cfopCode;

    private String ncmCode;

    private String cestCode;

    private String cnaeCode;

    private String serviceCode;

    private BigDecimal unitPrice;

    private BigDecimal quantity;

    @NotNull
    private BigDecimal totalValue;

    private BigDecimal discountValue;

    private BigDecimal freightValue;

    private BigDecimal insuranceValue;

    private BigDecimal otherExpensesValue;

    private String nfeModel; // 55, 65, 21, 22

    private String environment; // 1, 2

    private Map<String, Object> additionalData;
}