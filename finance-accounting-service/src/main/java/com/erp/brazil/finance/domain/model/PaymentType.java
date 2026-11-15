package com.erp.brazil.finance.domain.model;

public enum PaymentType {
    PAYABLE("Payable - Payment to Vendor"),
    RECEIVABLE("Receivable - Receipt from Customer"),
    TRANSFER("Transfer - Between Bank Accounts"),
    ADJUSTMENT("Adjustment - Bank Fee, Interest, etc."),
    REFUND_PAYABLE("Refund - Vendor Refund"),
    REFUND_RECEIVABLE("Refund - Customer Refund");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}