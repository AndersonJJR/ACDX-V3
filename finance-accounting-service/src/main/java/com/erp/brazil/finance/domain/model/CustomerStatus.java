package com.erp.brazil.finance.domain.model;

public enum CustomerStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    BLOCKED("Blocked"),
    SUSPENDED("Suspended"),
    PENDING_APPROVAL("Pending Approval");

    private final String description;

    CustomerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}