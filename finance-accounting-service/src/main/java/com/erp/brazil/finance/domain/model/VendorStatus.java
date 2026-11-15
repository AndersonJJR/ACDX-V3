package com.erp.brazil.finance.domain.model;

public enum VendorStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    BLOCKED("Blocked"),
    SUSPENDED("Suspended"),
    PENDING_APPROVAL("Pending Approval");

    private final String description;

    VendorStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}