package com.erp.brazil.finance.domain.model;

public enum PaymentStatus {
    PENDING("Pending"),
    AUTHORIZED("Authorized"),
    PROCESSING("Processing"),
    CLEARED("Cleared"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}