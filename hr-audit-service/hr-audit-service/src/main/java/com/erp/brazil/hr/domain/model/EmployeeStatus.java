package com.erp.brazil.hr.domain.model;

public enum EmployeeStatus {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    ON_LEAVE("Afastado"),
    TERMINATED("Desligado"),
    SUSPENDED("Suspenso");

    private final String description;

    EmployeeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}