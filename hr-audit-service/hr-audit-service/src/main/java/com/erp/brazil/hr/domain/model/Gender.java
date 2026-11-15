package com.erp.brazil.hr.domain.model;

public enum Gender {
    M("Masculino"),
    F("Feminino"),
    O("Outro");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}