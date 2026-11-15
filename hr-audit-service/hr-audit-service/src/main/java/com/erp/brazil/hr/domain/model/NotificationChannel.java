package com.erp.brazil.hr.domain.model;

public enum NotificationChannel {
    EMAIL("E-mail"),
    WHATSAPP("WhatsApp"),
    SMS("SMS"),
    PUSH("Push Notification"),
    IN_APP("In-App");

    private final String description;

    NotificationChannel(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean supportsRichContent() {
        return this == EMAIL || this == IN_APP;
    }
    public boolean requiresExternalProvider() {
        return this == WHATSAPP || this == SMS || this == PUSH;
    }
}