package com.erp.brazil.finance.domain.model;

public enum DocumentType {
    CPF("CPF"),
    CNPJ("CNPJ"),
    PASSPORT("Passport"),
    OTHER("Other");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}