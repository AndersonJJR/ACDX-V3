package com.erp.brazil.hr.domain.model;

public enum PayrollItemType {
    EARNING("Provento"),
    DEDUCTION("Desconto"),
    EMPLOYER_COST("Encargo Patronal"),
    INFORMATIONAL("Informativo");

    private final String description;

    PayrollItemType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean affectsNetSalary() {
        return this == EARNING || this == DEDUCTION;
    }
    public boolean isEmployerCost() {
        return this == EMPLOYER_COST;
    }
}