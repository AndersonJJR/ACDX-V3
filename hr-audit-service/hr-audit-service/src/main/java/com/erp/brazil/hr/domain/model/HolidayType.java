package com.erp.brazil.hr.domain.model;

public enum HolidayType {
    NATIONAL("Nacional"),
    STATE("Estadual"),
    MUNICIPAL("Municipal"),
    COMPANY("Empresa"),
    RELIGIOUS("Religioso"),
    OPTIONAL("Facultativo");

    private final String description;

    HolidayType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isMandatory() {
        return this != OPTIONAL && this != RELIGIOUS;
    }
}