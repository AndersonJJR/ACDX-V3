package com.erp.brazil.finance.domain.model;

public enum ReconciliationItemStatus {
    PENDING("Pending"),
    MATCHED("Matched"),
    UNMATCHED("Unmatched"),
    ADJUSTED("Adjusted"),
    IGNORED("Ignored");

    private final String description;

    ReconciliationItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}