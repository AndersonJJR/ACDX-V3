package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CertificateStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    REVOKED("Revoked"),
    PENDING_RENEWAL("Pending Renewal"),
    ERROR("Error");

    private final String description;
}