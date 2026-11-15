package com.erp.brazil.finance.domain.model;

public enum ReconciliationStatus {
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    DISCREPANCY("Discrepancy Found");

    private final String description;

    ReconciliationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}