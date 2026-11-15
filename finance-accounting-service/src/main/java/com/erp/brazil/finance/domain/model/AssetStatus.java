package com.erp.brazil.finance.domain.model;

public enum AssetStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    UNDER_MAINTENANCE("Under Maintenance"),
    DISPOSED("Disposed"),
    SOLD("Sold"),
    TRANSFERRED("Transferred"),
    FULLY_DEPRECIATED("Fully Depreciated");

    private final String description;

    AssetStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}