package com.erp.brazil.finance.domain.model;

public enum DocumentType {
    CPF("CPF - Individual Taxpayer Registry"),
    CNPJ("CNPJ - National Registry of Legal Entities"),
    PASSPORT("Passport"),
    FOREIGN_ID("Foreign ID"),
    OTHER("Other");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}