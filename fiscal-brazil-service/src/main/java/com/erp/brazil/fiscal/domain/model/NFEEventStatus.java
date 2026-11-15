package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFEEventStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    AUTHORIZED("Authorized"),
    REJECTED("Rejected"),
    ERROR("Error");

    private final String description;
}