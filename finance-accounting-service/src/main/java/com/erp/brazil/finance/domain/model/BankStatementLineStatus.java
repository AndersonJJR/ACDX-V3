package com.erp.brazil.finance.domain.model;

public enum BankStatementLineStatus {
    UNRECONCILED("Unreconciled"),
    MATCHED("Matched"),
    RECONCILED("Reconciled"),
    IGNORED("Ignored"),
    DISPUTED("Disputed");

    private final String description;

    BankStatementLineStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}