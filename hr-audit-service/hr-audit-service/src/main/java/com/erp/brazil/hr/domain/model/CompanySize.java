package com.erp.brazil.hr.domain.model;

public enum CompanySize {
    MEI("MEI"),
    MICRO("Microempresa"),
    SMALL("Pequena Empresa"),
    MEDIUM("Média Empresa"),
    LARGE("Grande Empresa");

    private final String description;

    CompanySize(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isSimplesNacionalEligible() {
        return this == MEI || this == MICRO || this == SMALL;
    }
}