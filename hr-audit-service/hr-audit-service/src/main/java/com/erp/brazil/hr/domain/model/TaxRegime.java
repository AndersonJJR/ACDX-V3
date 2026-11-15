package com.erp.brazil.hr.domain.model;

public enum TaxRegime {
    SIMPLES_NACIONAL("Simples Nacional"),
    LUCRO_PRESUMIDO("Lucro Presumido"),
    LUCRO_REAL("Lucro Real");

    private final String description;

    TaxRegime(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}