package com.erp.brazil.hr.domain.model;

public enum DependentRelationship {
    SPOUSE("Cônjuge/Companheiro(a)"),
    CHILD("Filho(a)"),
    STEPCHILD("Enteado(a)"),
    PARENT("Pai/Mãe"),
    GRANDPARENT("Avô/Avó"),
    SIBLING("Irmão(ã)"),
    OTHER("Outro");

    private final String description;

    DependentRelationship(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isTaxDependentByDefault() {
        return this == SPOUSE || this == CHILD || this == STEPCHILD;
    }
    public boolean isHealthDependentByDefault() {
        return this == SPOUSE || this == CHILD || this == STEPCHILD || this == PARENT;
    }
}