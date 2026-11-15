package com.erp.brazil.hr.domain.model;

public enum DeliveryMethod {
    EMAIL("E-mail"),
    PORTAL("Portal"),
    PRINTED("Impresso"),
    WHATSAPP("WhatsApp");

    private final String description;

    DeliveryMethod(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}