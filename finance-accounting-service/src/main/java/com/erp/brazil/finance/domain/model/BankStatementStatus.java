package com.erp.brazil.finance.domain.model;

public enum BankStatementStatus {
    IMPORTED("Imported"),
    PROCESSING("Processing"),
    MATCHED("Matched"),
    PARTIALLY_MATCHED("Partially Matched"),
    ERROR("Error");

    private final String description;

    BankStatementStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}