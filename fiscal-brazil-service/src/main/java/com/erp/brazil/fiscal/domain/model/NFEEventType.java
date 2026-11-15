package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFEEventType {
    CANCELLATION("110111", "Cancellation"),
    CORRECTION_LETTER("110110", "Correction Letter"),
    RECIPIENT_MANIFESTATION("210200", "Recipient Manifestation"),
    EPEC("110140", "EPEC - Pre-Emission Event in Contingency"),
    ECC("110110", "Electronic Correction Letter");

    private final String code;
    private final String description;
}