package com.erp.brazil.finance.domain.model;

public enum InvoiceStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled"),
    DISPUTED("Disputed");

    private final String description;

    InvoiceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}