package com.erp.brazil.fiscal.infrastructure.taxrule;

import com.erp.brazil.fiscal.application.port.TaxCalculationPort;
import com.erp.brazil.fiscal.domain.model.*;
import com.erp.brazil.fiscal.domain.repository.NCMRepository;
import com.erp.brazil.fiscal.domain.repository.CESTRepository;
import com.erp.brazil.fiscal.domain.repository.CFOPRepository;
import com.erp.brazil.fiscal.domain.repository.TaxRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationService implements TaxCalculationPort {

    private final TaxRuleRepository taxRuleRepository;
    private final NCMRepository ncmRepository;
    private final CESTRepository cestRepository;
    private final CFOPRepository cfopRepository;

    @Override
    @Cacheable(value = "taxCalculation", key = "#context.companyCnpj + '_' + #context.operationType + '_' + #context.cfopCode + '_' + #context.ncmCode + '_' + #context.totalValue")
    public TaxCalculationResult calculate(TaxCalculationContext context) {
        log.debug("Calculating taxes for context: {}", context);

        TaxCalculationResult result = TaxCalculationResult.builder()
                .appliedRules(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        LocalDate calculationDate = LocalDate.now();

        // Calculate ICMS
        calculateICMS(context, result, calculationDate);

        // Calculate IPI
        calculateIPI(context, result, calculationDate);

        // Calculate PIS
        calculatePIS(context, result, calculationDate);

        // Calculate COFINS
        calculateCOFINS(context, result, calculationDate);

        // Calculate ISS (for services)
        if ("SERVICO".equalsIgnoreCase(context.getOperationType())) {
            calculateISS(context, result, calculationDate);
        }

        // Calculate Retentions (IRRF, CSLL, PIS, COFINS, INSS)
        calculateRetentions(context, result, calculationDate);

        // Compute totals
        computeTotals(result);

        return result;
    }

    private void calculateICMS(TaxCalculationContext context, TaxCalculationResult result, LocalDate date) {
        List<ICMSRule> rules = taxRuleRepository.findMatchingICMSRules(
                date,
                context.getCompanyState(),
                context.getPartnerState(),
                context.getNcmCode(),
                context.getCestCode(),
                context.getCfopCode(),
                context.getTaxRegime() != null ? context.getTaxRegime().name() : null
        );

        if (rules.isEmpty()) {
            result.getWarnings().add("No ICMS rule found for the given context");
            return;
        }

        ICMSRule rule = rules.get(0);
        result.getAppliedRules().add("ICMS:" + rule.getId());

        BigDecimal baseValue = context.getTotalValue();
        if (context.getDiscountValue() != null) {
            baseValue = baseValue.subtract(context.getDiscountValue());
        }
        if (context.getFreightValue() != null) {
            baseValue = baseValue.add(context.getFreightValue());
        }
        if (context.getInsuranceValue() != null) {
            baseValue = baseValue.add(context.getInsuranceValue());
        }
        if (context.getOtherExpensesValue() != null) {
            baseValue = baseValue.add(context.getOtherExpensesValue());
        }

        BigDecimal icmsValue = rule.calculate(baseValue, context);

        result.setBaseIcms(baseValue.setScale(2, RoundingMode.HALF_UP));
        result.setValueIcms(icmsValue);
        result.setIcmsCst(rule.getCst());
        result.setIcmsCsosn(rule.getCsosn());
        result.setIcmsModalidadeBc(rule.getModalidadeBc());

        // ST
        if (rule.isSt()) {
            result.setBaseIcmsSt(baseValue.setScale(2, RoundingMode.HALF_UP));
            result.setValueIcmsSt(rule.calculate(baseValue, context).subtract(icmsValue).max(BigDecimal.ZERO));
        }

        // FCP
        if (rule.isFcp()) {
            result.setValueFcp(baseValue.multiply(rule.getAliquotaFcp() != null ? rule.getAliquotaFcp() : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }
    }

    private void calculateIPI(TaxCalculationContext context, TaxCalculationResult result, LocalDate date) {
        List<IPIRule> rules = taxRuleRepository.findMatchingIPIRules(date, context.getNcmCode());

        if (rules.isEmpty()) {
            return;
        }

        IPIRule rule = rules.get(0);
        result.getAppliedRules().add("IPI:" + rule.getId());

        BigDecimal baseValue = context.getTotalValue();
        if (context.getDiscountValue() != null) {
            baseValue = baseValue.subtract(context.getDiscountValue());
        }
        if (context.getFreightValue() != null) {
            baseValue = baseValue.add(context.getFreightValue());
        }
        if (context.getInsuranceValue() != null) {
            baseValue = baseValue.add(context.getInsuranceValue());
        }

        BigDecimal ipiValue = rule.calculate(baseValue, context);

        result.setBaseIpi(baseValue.setScale(2, RoundingMode.HALF_UP));
        result.setValueIpi(ipiValue);
        result.setIpiCst(rule.getCst());
    }

    private void calculatePIS(TaxCalculationContext context, TaxCalculationResult result, LocalDate date) {
        List<PISRule> rules = taxRuleRepository.findMatchingPISRules(
                date, context.getNcmCode(), context.getCfopCode(),
                context.getTaxRegime() != null ? context.getTaxRegime().name() : null
        );

        if (rules.isEmpty()) {
            return;
        }

        PISRule rule = rules.get(0);
        result.getAppliedRules().add("PIS:" + rule.getId());

        BigDecimal baseValue = context.getTotalValue();
        BigDecimal pisValue = rule.calculate(baseValue, context);

        result.setBasePis(baseValue.setScale(2, RoundingMode.HALF_UP));
        result.setValuePis(pisValue);
        result.setPisCst(rule.getCst());
    }

    private void calculateCOFINS(TaxCalculationContext context, TaxCalculationResult result, LocalDate date) {
        List<COFINSRule> rules = taxRuleRepository.findMatchingCOFINSRules(
                date, context.getNcmCode(), context.getCfopCode(),
                context.getTaxRegime() != null ? context.getTaxRegime().name() : null
        );

        if (rules.isEmpty()) {
            return;
        }

        COFINSRule rule = rules.get(0);
        result.getAppliedRules().add("COFINS:" + rule.getId());

        BigDecimal baseValue = context.getTotalValue();
        BigDecimal cofinsValue = rule.calculate(baseValue, context);

        result.setBaseCofins(baseValue.setScale(2, RoundingMode.HALF_UP));
        result.setValueCofins(cofinsValue);
        result.setCofinsCst(rule.getCst());
    }

    private void calculateISS(TaxCalculationContext context, TaxCalculationResult result, LocalDate date) {
        List<ISSRule> rules = taxRuleRepository.findMatchingISSRules(
                date, context.getCompanyMunicipality(), context.getServiceCode(), context.getCnaeCode()
        );

        if (rules.isEmpty()) {
            return;
        }

        ISSRule rule = rules.get(0);
        result.getAppliedRules().add("ISS:" + rule.getId());

        BigDecimal baseValue = context.getTotalValue();
        BigDecimal issValue = rule.calculate(baseValue, context);

        result.setBaseIss(baseValue.setScale(2, RoundingMode.HALF_UP));
        result.setValueIss(issValue);
        result.setIssCalculationType(rule.getDeductionType());
    }

    private void calculateRetentions(TaxCalculationContext context, TaxCalculationResult result, LocalDate date) {
        // This would integrate with RetencaoCalculationPort
        // For now, placeholder - actual implementation in CalculateRetencoesUseCase
        result.setValueIrrf(BigDecimal.ZERO);
        result.setValueCsll(BigDecimal.ZERO);
        result.setValueInss(BigDecimal.ZERO);
    }

    private void computeTotals(TaxCalculationResult result) {
        BigDecimal totalTaxes = BigDecimal.ZERO;

        if (result.getValueIcms() != null) totalTaxes = totalTaxes.add(result.getValueIcms());
        if (result.getValueIcmsSt() != null) totalTaxes = totalTaxes.add(result.getValueIcmsSt());
        if (result.getValueFcp() != null) totalTaxes = totalTaxes.add(result.getValueFcp());
        if (result.getValueFcpSt() != null) totalTaxes = totalTaxes.add(result.getValueFcpSt());
        if (result.getValueFcpStRet() != null) totalTaxes = totalTaxes.add(result.getValueFcpStRet());
        if (result.getValueIpi() != null) totalTaxes = totalTaxes.add(result.getValueIpi());
        if (result.getValuePis() != null) totalTaxes = totalTaxes.add(result.getValuePis());
        if (result.getValueCofins() != null) totalTaxes = totalTaxes.add(result.getValueCofins());
        if (result.getValueIss() != null) totalTaxes = totalTaxes.add(result.getValueIss());
        if (result.getValueIrrf() != null) totalTaxes = totalTaxes.add(result.getValueIrrf());
        if (result.getValueCsll() != null) totalTaxes = totalTaxes.add(result.getValueCsll());
        if (result.getValueInss() != null) totalTaxes = totalTaxes.add(result.getValueInss());

        result.setTotalTaxes(totalTaxes.setScale(2, RoundingMode.HALF_UP));
        result.setTotalValueWithTaxes(
                (result.getBaseIcms() != null ? result.getBaseIcms() : BigDecimal.ZERO)
                        .add(totalTaxes)
                        .setScale(2, RoundingMode.HALF_UP)
        );
    }
}