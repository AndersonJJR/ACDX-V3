package com.erp.brazil.sales.domain.model;

public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    AUTHORIZED,
    CANCELLED,
    DENIED,
    PAID,
    PARTIALLY_PAID,
    OVERDUE
}