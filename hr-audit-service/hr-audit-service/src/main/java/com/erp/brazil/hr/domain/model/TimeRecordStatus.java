package com.erp.brazil.hr.domain.model;

public enum TimeRecordStatus {
    PENDING("Pendente"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    DISPUTED("Contestado");

    private final String description;

    TimeRecordStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}