package com.erp.brazil.finance.domain.model;

public enum PaymentMethodType {
    BANK_TRANSFER("Bank Transfer"),
    PIX("PIX"),
    BOLETO("Boleto"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CASH("Cash"),
    CHECK("Check"),
    WIRE_TRANSFER("Wire Transfer"),
    DIRECT_DEBIT("Direct Debit"),
    DIGITAL_WALLET("Digital Wallet"),
    CRYPTOCURRENCY("Cryptocurrency"),
    OTHER("Other");

    private final String description;

    PaymentMethodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}