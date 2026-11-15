package com.erp.brazil.finance.domain.model;

public enum JournalEntryStatus {
    DRAFT("Draft"),
    POSTED("Posted"),
    REVERSED("Reversed"),
    CANCELLED("Cancelled");

    private final String description;

    JournalEntryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}