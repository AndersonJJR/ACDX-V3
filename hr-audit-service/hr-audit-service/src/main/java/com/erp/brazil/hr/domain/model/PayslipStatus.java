package com.erp.brazil.hr.domain.model;

public enum PayslipStatus {
    GENERATED("Gerado"),
    SIGNED("Assinado"),
    DELIVERED("Entregue"),
    VIEWED("Visualizado"),
    DOWNLOADED("Baixado");

    private final String description;

    PayslipStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}