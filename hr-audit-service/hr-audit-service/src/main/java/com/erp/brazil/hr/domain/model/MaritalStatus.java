package com.erp.brazil.hr.domain.model;

public enum MaritalStatus {
    SINGLE("Solteiro(a)"),
    MARRIED("Casado(a)"),
    DIVORCED("Divorciado(a)"),
    WIDOWED("Viúvo(a)"),
    STABLE_UNION("União Estável");

    private final String description;

    MaritalStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}