package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SPEDStatus {
    GENERATED("Generated"),
    VALIDATED("Validated"),
    WITH_ERRORS("With Errors"),
    TRANSMITTED("Transmitted"),
    AUTHORIZED("Authorized"),
    REJECTED("Rejected");

    private final String description;
}