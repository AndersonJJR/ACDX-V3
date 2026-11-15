package com.erp.brazil.finance.domain.model;

public enum DepreciationMethod {
    STRAIGHT_LINE("Straight Line"),
    DECLINING_BALANCE("Declining Balance"),
    DOUBLE_DECLINING_BALANCE("Double Declining Balance"),
    SUM_OF_YEARS_DIGITS("Sum of Years Digits"),
    UNITS_OF_PRODUCTION("Units of Production");

    private final String description;

    DepreciationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}