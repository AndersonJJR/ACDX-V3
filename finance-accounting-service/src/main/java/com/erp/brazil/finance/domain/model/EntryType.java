package com.erp.brazil.finance.domain.model;

public enum EntryType {
    DEBIT("Debit"),
    CREDIT("Credit");

    private final String description;

    EntryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}