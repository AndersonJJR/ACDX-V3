package com.erp.brazil.finance.domain.model;

public enum DepreciationScheduleStatus {
    PENDING("Pending"),
    POSTED("Posted"),
    SKIPPED("Skipped"),
    CANCELLED("Cancelled");

    private final String description;

    DepreciationScheduleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}