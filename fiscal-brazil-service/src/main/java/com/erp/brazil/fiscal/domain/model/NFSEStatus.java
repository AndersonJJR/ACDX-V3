package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFSEStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    AUTHORIZED("Authorized"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    REPLACED("Replaced"),
    ERROR("Error");

    private final String description;
}