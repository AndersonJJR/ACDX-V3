package com.erp.brazil.hr.domain.model;

public enum WorkLocation {
    ONSITE("Presencial"),
    REMOTE("Remoto"),
    HYBRID("Híbrido");

    private final String description;

    WorkLocation(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}