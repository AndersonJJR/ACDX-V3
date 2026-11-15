package com.erp.brazil.finance.domain.model;

public enum AccountNature {
    DEBIT("Debit"),
    CREDIT("Credit");

    private final String description;

    AccountNature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}