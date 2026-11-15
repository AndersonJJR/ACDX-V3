package com.erp.brazil.finance.domain.model;

public enum PaymentTermType {
    NET_DAYS("Net Days"),
    END_OF_MONTH("End of Month"),
    FIXED_DATE("Fixed Date"),
    IMMEDIATE("Immediate"),
    CUSTOM("Custom");

    private final String description;

    PaymentTermType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}