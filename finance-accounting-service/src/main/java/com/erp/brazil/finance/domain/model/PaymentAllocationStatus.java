package com.erp.brazil.finance.domain.model;

public enum PaymentAllocationStatus {
    PENDING("Pending"),
    APPLIED("Applied"),
    PARTIALLY_APPLIED("Partially Applied"),
    REVERSED("Reversed"),
    CANCELLED("Cancelled");

    private final String description;

    PaymentAllocationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}