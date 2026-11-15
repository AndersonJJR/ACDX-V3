package com.erp.brazil.finance.domain.model;

public enum ReconciliationItemType {
    BANK_STATEMENT_LINE("Bank Statement Line"),
    GL_ENTRY("General Ledger Entry"),
    ADJUSTMENT("Adjustment"),
    OUTSTANDING_CHECK("Outstanding Check"),
    DEPOSIT_IN_TRANSIT("Deposit in Transit"),
    BANK_FEE("Bank Fee"),
    INTEREST_EARNED("Interest Earned"),
    ERROR_CORRECTION("Error Correction");

    private final String description;

    ReconciliationItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}