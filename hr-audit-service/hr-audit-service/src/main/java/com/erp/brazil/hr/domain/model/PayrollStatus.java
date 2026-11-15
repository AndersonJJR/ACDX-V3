package com.erp.brazil.hr.domain.model;

public enum PayrollStatus {
    DRAFT("Rascunho"),
    CALCULATING("Calculando"),
    CALCULATED("Calculado"),
    APPROVED("Aprovado"),
    PROCESSING("Processando"),
    PROCESSED("Processado"),
    PAID("Pago"),
    CANCELLED("Cancelado"),
    ERROR("Erro");

    private final String description;

    PayrollStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isEditable() {
        return this == DRAFT;
    }
    public boolean isFinal() {
        return this == PROCESSED || this == PAID || this == CANCELLED;
    }
    public boolean canTransitionTo(PayrollStatus newStatus) {
        return switch (this) {
            case DRAFT -> newStatus == CALCULATING || newStatus == CANCELLED;
            case CALCULATING -> newStatus == CALCULATED || newStatus == ERROR || newStatus == CANCELLED;
            case CALCULATED -> newStatus == APPROVED || newStatus == DRAFT || newStatus == CANCELLED;
            case APPROVED -> newStatus == PROCESSING || newStatus == CALCULATED || newStatus == CANCELLED;
            case PROCESSING -> newStatus == PROCESSED || newStatus == ERROR || newStatus == CANCELLED;
            case PROCESSED -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == CANCELLED;
            case ERROR -> newStatus == CALCULATING || newStatus == CANCELLED;
            case CANCELLED -> false;
        };
    }
}