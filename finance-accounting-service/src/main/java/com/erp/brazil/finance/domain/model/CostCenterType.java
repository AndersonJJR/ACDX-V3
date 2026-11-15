package com.erp.brazil.finance.domain.model;

public enum CostCenterType {
    PRODUCTION("Production"),
    ADMINISTRATIVE("Administrative"),
    COMMERCIAL("Commercial"),
    FINANCIAL("Financial"),
    LOGISTICS("Logistics"),
    PROJECT("Project"),
    RESEARCH_DEVELOPMENT("Research & Development"),
    QUALITY("Quality"),
    MAINTENANCE("Maintenance"),
    OTHER("Other");

    private final String description;

    CostCenterType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}