package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFEStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    AUTHORIZED("Authorized"),
    AUTHORIZED_WITH_WARNING("Authorized with Warning"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    INUTILIZED("Inutilized"),
    DENIED("Denied"),
    ERROR("Error"),
    CONTINGENCY("Contingency");

    private final String description;
}