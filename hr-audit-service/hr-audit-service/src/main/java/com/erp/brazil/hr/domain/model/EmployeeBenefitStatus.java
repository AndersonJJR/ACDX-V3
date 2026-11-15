package com.erp.brazil.hr.domain.model;

public enum EmployeeBenefitStatus {
    ACTIVE("Ativo"),
    SUSPENDED("Suspenso"),
    CANCELLED("Cancelado"),
    EXPIRED("Expirado");

    private final String description;

    EmployeeBenefitStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}