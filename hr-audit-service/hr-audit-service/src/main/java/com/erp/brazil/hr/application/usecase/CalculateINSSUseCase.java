package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.PayrollItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class CalculateINSSUseCase {

    private static final BigDecimal[] BRACKETS = {
        new BigDecimal("1412.00"),
        new BigDecimal("2666.68"),
        new BigDecimal("4000.03"),
        new BigDecimal("7786.02")
    };

    private static final BigDecimal[] RATES = {
        new BigDecimal("0.075"),
        new BigDecimal("0.09"),
        new BigDecimal("0.12"),
        new BigDecimal("0.14")
    };

    private static final BigDecimal CEILING = new BigDecimal("908.86");

    public BigDecimal calculate(BigDecimal baseSalary) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal inss = BigDecimal.ZERO;
        BigDecimal previousCeiling = BigDecimal.ZERO;

        for (int i = 0; i < BRACKETS.length; i++) {
            BigDecimal bracketCeiling = BRACKETS[i];
            BigDecimal rate = RATES[i];

            if (baseSalary.compareTo(bracketCeiling) <= 0) {
                inss = inss.add(baseSalary.subtract(previousCeiling).multiply(rate));
                break;
            } else {
                inss = inss.add(bracketCeiling.subtract(previousCeiling).multiply(rate));
                previousCeiling = bracketCeiling;
            }
        }

        if (inss.compareTo(CEILING) > 0) {
            return CEILING.setScale(2, RoundingMode.HALF_UP);
        }

        return inss.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEmployer(BigDecimal baseSalary) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return baseSalary.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
    }

    public void applyToPayrollItems(List<PayrollItem> items, BigDecimal baseSalary) {
        BigDecimal employeeInss = calculate(baseSalary);
        BigDecimal employerInss = calculateEmployer(baseSalary);

        items.add(PayrollItem.builder()
            .category(PayrollItem.Category.INSS_EMPLOYEE)
            .itemType(PayrollItem.ItemType.DEDUCTION)
            .code("INSS_EMP")
            .description("INSS - Employee Contribution")
            .totalAmount(employeeInss)
            .taxableInss(false)
            .taxableIrrf(true)
            .calculationBasis("Base: " + baseSalary + " | Brackets: 7.5%/9%/12%/14%")
            .build());

        items.add(PayrollItem.builder()
            .category(PayrollItem.Category.INSS_EMPLOYER)
            .itemType(PayrollItem.ItemType.EMPLOYER_COST)
            .code("INSS_EMPR")
            .description("INSS - Employer Contribution (20%)")
            .totalAmount(employerInss)
            .taxableInss(false)
            .taxableIrrf(false)
            .calculationBasis("Base: " + baseSalary + " | Rate: 20%")
            .build());
    }
}