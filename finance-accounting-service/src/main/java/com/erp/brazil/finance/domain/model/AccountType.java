package com.erp.brazil.finance.domain.model;

public enum AccountType {
    ASSET("Asset"),
    LIABILITY("Liability"),
    EQUITY("Equity"),
    REVENUE("Revenue"),
    EXPENSE("Expense");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}