package com.erp.brazil.hr.domain.model;

public enum NotificationStatus {
    QUEUED("Na Fila"),
    SENDING("Enviando"),
    SENT("Enviado"),
    DELIVERED("Entregue"),
    READ("Lido"),
    FAILED("Falhou"),
    CANCELLED("Cancelado");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isFinal() {
        return this == DELIVERED || this == READ || this == FAILED || this == CANCELLED;
    }
    public boolean canRetry() {
        return this == FAILED;
    }
}