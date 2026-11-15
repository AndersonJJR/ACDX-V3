package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.PayrollItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class CalculateIRRFUseCase {

    private static final BigDecimal[] BRACKETS = {
        new BigDecimal("2259.20"),
        new BigDecimal("2826.65"),
        new BigDecimal("3751.05"),
        new BigDecimal("4664.68")
    };

    private static final BigDecimal[] RATES = {
        BigDecimal.ZERO,
        new BigDecimal("0.075"),
        new BigDecimal("0.15"),
        new BigDecimal("0.225"),
        new BigDecimal("0.275")
    };

    private static final BigDecimal[] DEDUCTIONS = {
        BigDecimal.ZERO,
        new BigDecimal("169.44"),
        new BigDecimal("381.44"),
        new BigDecimal("662.77"),
        new BigDecimal("896.00")
    };

    private static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("189.59");

    public BigDecimal calculate(BigDecimal taxableBase, int dependents) {
        if (taxableBase == null || taxableBase.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal dependentDeduction = DEPENDENT_DEDUCTION.multiply(BigDecimal.valueOf(dependents));
        BigDecimal calculationBase = taxableBase.subtract(dependentDeduction);

        if (calculationBase.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal irrf = BigDecimal.ZERO;
        BigDecimal previousCeiling = BigDecimal.ZERO;

        for (int i = 0; i < BRACKETS.length; i++) {
            BigDecimal bracketCeiling = BRACKETS[i];
            BigDecimal rate = RATES[i];
            BigDecimal deduction = DEDUCTIONS[i];

            if (calculationBase.compareTo(bracketCeiling) <= 0) {
                irrf = calculationBase.multiply(rate).subtract(deduction);
                break;
            }
            previousCeiling = bracketCeiling;
        }

        if (calculationBase.compareTo(BRACKETS[BRACKETS.length - 1]) > 0) {
            BigDecimal rate = RATES[RATES.length - 1];
            BigDecimal deduction = DEDUCTIONS[DEDUCTIONS.length - 1];
            irrf = calculationBase.multiply(rate).subtract(deduction);
        }

        return irrf.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public void applyToPayrollItems(List<PayrollItem> items, BigDecimal taxableBase, int dependents) {
        BigDecimal irrf = calculate(taxableBase, dependents);

        items.add(PayrollItem.builder()
            .category(PayrollItem.Category.IRRF)
            .itemType(PayrollItem.ItemType.DEDUCTION)
            .code("IRRF")
            .description("IRRF - Income Tax Withholding")
            .totalAmount(irrf)
            .taxableInss(false)
            .taxableIrrf(false)
            .calculationBasis("Taxable base: " + taxableBase + " | Dependents: " + dependents + " (deduction: " + DEPENDENT_DEDUCTION.multiply(BigDecimal.valueOf(dependents)) + ")")
            .build());
    }
}