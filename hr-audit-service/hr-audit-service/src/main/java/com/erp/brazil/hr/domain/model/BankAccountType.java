package com.erp.brazil.hr.domain.model;

public enum BankAccountType {
    CHECKING("Conta Corrente"),
    SAVINGS("Conta Poupança");

    private final String description;

    BankAccountType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}