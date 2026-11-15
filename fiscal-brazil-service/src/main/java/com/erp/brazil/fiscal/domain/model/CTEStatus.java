package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CTEStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    AUTHORIZED("Authorized"),
    AUTHORIZED_WITH_WARNING("Authorized with Warning"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    INUTILIZED("Inutilized"),
    ERROR("Error"),
    CONTINGENCY("Contingency");

    private final String description;
}