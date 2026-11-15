package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PayrollItem(
    Long id,
    UUID uuid,
    Long payrollId,
    Long employeeId,
    ItemType itemType,
    Category category,
    String code,
    String description,
    BigDecimal referenceValue,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal totalAmount,
    boolean taxableInss,
    boolean taxableIrrf,
    boolean taxableFgts,
    boolean isFixed,
    String calculationBasis,
    int displayOrder
) {
    public enum ItemType {
        EARNING, DEDUCTION, EMPLOYER_COST, INFORMATIONAL
    }

    public enum Category {
        SALARY_BASE, OVERTIME, NIGHT_SHIFT, HAZARDOUS, UNHEALTHY,
        VACATION, THIRTEENTH, NOTICE_PERIOD, FGTS, INSS_EMPLOYEE, INSS_EMPLOYER,
        IRRF, TRANSPORT_VOUCHER, MEAL_VOUCHER, HEALTH_INSURANCE, DENTAL_INSURANCE,
        LIFE_INSURANCE, UNION_DUES, ADVANCE, LOAN, GARNISHMENT, TAX, OTHER
    }

    public boolean isEarning() {
        return itemType == ItemType.EARNING;
    }

    public boolean isDeduction() {
        return itemType == ItemType.DEDUCTION;
    }

    public boolean isEmployerCost() {
        return itemType == ItemType.EMPLOYER_COST;
    }
}