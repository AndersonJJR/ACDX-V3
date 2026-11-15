package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntegracaoStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ERROR("Error"),
    MAINTENANCE("Maintenance"),
    TEST("Test");

    private final String description;
}