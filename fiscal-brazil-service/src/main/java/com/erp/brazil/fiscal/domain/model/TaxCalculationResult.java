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
public class TaxCalculationResult {
    private BigDecimal baseIcms;
    private BigDecimal valueIcms;
    private BigDecimal baseIcmsSt;
    private BigDecimal valueIcmsSt;
    private BigDecimal valueFcp;
    private BigDecimal valueFcpSt;
    private BigDecimal valueFcpStRet;
    private BigDecimal baseIpi;
    private BigDecimal valueIpi;
    private BigDecimal basePis;
    private BigDecimal valuePis;
    private BigDecimal baseCofins;
    private BigDecimal valueCofins;
    private BigDecimal baseIss;
    private BigDecimal valueIss;
    private BigDecimal valueIrrf;
    private BigDecimal valueCsll;
    private BigDecimal valueInss;
    private BigDecimal totalTaxes;
    private BigDecimal totalValueWithTaxes;

    private String icmsCst;
    private String icmsCsosn;
    private String icmsModalidadeBc;
    private String ipiCst;
    private String pisCst;
    private String cofinsCst;
    private String issCalculationType;

    private List<String> appliedRules;
    private List<String> warnings;
    private Map<String, Object> details;

    public BigDecimal getTotalFederalTaxes() {
        BigDecimal total = BigDecimal.ZERO;
        if (valueIpi != null) total = total.add(valueIpi);
        if (valuePis != null) total = total.add(valuePis);
        if (valueCofins != null) total = total.add(valueCofins);
        if (valueIrrf != null) total = total.add(valueIrrf);
        if (valueCsll != null) total = total.add(valueCsll);
        if (valueInss != null) total = total.add(valueInss);
        return total;
    }

    public BigDecimal getTotalStateTaxes() {
        BigDecimal total = BigDecimal.ZERO;
        if (valueIcms != null) total = total.add(valueIcms);
        if (valueIcmsSt != null) total = total.add(valueIcmsSt);
        if (valueFcp != null) total = total.add(valueFcp);
        if (valueFcpSt != null) total = total.add(valueFcpSt);
        if (valueFcpStRet != null) total = total.add(valueFcpStRet);
        return total;
    }

    public BigDecimal getTotalMunicipalTaxes() {
        return valueIss != null ? valueIss : BigDecimal.ZERO;
    }
}