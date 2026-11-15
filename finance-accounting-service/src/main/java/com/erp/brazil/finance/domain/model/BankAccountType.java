package com.erp.brazil.finance.domain.model;

public enum BankAccountType {
    CHECKING("Checking Account"),
    SAVINGS("Savings Account"),
    SALARY("Salary Account"),
    INVESTMENT("Investment Account");

    private final String description;

    BankAccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}