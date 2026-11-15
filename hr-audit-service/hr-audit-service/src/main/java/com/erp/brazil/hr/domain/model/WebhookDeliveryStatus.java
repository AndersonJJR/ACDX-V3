package com.erp.brazil.hr.domain.model;

public enum WebhookDeliveryStatus {
    PENDING("Pendente"),
    SENT("Enviado"),
    SUCCESS("Sucesso"),
    FAILED("Falhou"),
    RETRYING("Tentando Novamente"),
    DEAD_LETTER("Fila de Mortos");

    private final String description;

    WebhookDeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isTerminal() {
        return this == SUCCESS || this == DEAD_LETTER;
    }
    public boolean canRetry() {
        return this == FAILED || this == RETRYING;
    }
}