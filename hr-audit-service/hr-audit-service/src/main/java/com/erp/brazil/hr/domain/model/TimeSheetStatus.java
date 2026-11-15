package com.erp.brazil.hr.domain.model;

public enum TimeSheetStatus {
    OPEN("Aberto"),
    CALCULATED("Calculado"),
    APPROVED("Aprovado"),
    CLOSED("Fechado"),
    DISPUTED("Contestado");

    private final String description;

    TimeSheetStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isEditable() {
        return this == OPEN || this == CALCULATED || this == DISPUTED;
    }
    public boolean isFinal() {
        return this == APPROVED || this == CLOSED;
    }
}