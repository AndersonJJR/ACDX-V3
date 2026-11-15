package com.erp.brazil.hr.domain.model;

public enum NotificationPriority {
    LOW("Baixa"),
    NORMAL("Normal"),
    HIGH("Alta"),
    URGENT("Urgente");

    private final String description;
    private final int order;

    NotificationPriority(String description) {
        this.description = description;
        this.order = ordinal();
    }

    public String getDescription() { return description; }
    public int getOrder() { return order; }
}