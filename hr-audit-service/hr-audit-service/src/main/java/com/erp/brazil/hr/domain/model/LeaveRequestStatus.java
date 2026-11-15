package com.erp.brazil.hr.domain.model;

public enum LeaveRequestStatus {
    PENDING("Pendente"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado"),
    CANCELLED("Cancelado"),
    EXPIRED("Expirado");

    private final String description;

    LeaveRequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == CANCELLED || this == EXPIRED;
    }
    public boolean canBeCancelled() {
        return this == PENDING || this == APPROVED;
    }
}