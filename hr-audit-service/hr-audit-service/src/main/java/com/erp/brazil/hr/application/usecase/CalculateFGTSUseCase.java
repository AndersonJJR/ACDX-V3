package com.erp.brazil.hr.application.usecase;

import com.erp.brazil.hr.domain.model.PayrollItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class CalculateFGTSUseCase {

    private static final BigDecimal FGTS_RATE = new BigDecimal("0.08");
    private static final BigDecimal FGTS_MONTHLY_DEPOSIT_RATE = new BigDecimal("0.032"); // 8% * 40% for resignation

    public BigDecimal calculateMonthlyDeposit(BigDecimal baseSalary) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return baseSalary.multiply(FGTS_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateResignationFine(BigDecimal baseSalary, int monthsWorked) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0 || monthsWorked <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal monthlyDeposit = calculateMonthlyDeposit(baseSalary);
        return monthlyDeposit.multiply(BigDecimal.valueOf(monthsWorked)).multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);
    }

    public void applyToPayrollItems(List<PayrollItem> items, BigDecimal baseSalary) {
        BigDecimal fgts = calculateMonthlyDeposit(baseSalary);

        items.add(PayrollItem.builder()
            .category(PayrollItem.Category.FGTS)
            .itemType(PayrollItem.ItemType.EMPLOYER_COST)
            .code("FGTS")
            .description("FGTS - Monthly Deposit (8%)")
            .totalAmount(fgts)
            .taxableInss(false)
            .taxableIrrf(false)
            .taxableFgts(false)
            .calculationBasis("Base: " + baseSalary + " | Rate: 8%")
            .build());
    }
}