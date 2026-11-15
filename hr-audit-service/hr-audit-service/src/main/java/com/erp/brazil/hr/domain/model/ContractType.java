package com.erp.brazil.hr.domain.model;

public enum ContractType {
    CLT_INDEFINITE("CLT Por Tempo Indeterminado"),
    CLT_FIXED_TERM("CLT Por Tempo Determinado"),
    CLT_TEMPORARY("CLT Temporário"),
    APPRENTICE("Aprendiz"),
    INTERNSHIP("Estágio"),
    STATUTORY("Estatutário"),
    COOPERATIVE("Cooperado");

    private final String description;

    ContractType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isClt() {
        return this == CLT_INDEFINITE || this == CLT_FIXED_TERM || this == CLT_TEMPORARY;
    }

    public boolean hasFgts() {
        return isClt();
    }

    public boolean hasNoticePeriod() {
        return this == CLT_INDEFINITE || this == CLT_FIXED_TERM;
    }
}