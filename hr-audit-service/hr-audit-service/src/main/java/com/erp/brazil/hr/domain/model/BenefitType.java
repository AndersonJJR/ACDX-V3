package com.erp.brazil.hr.domain.model;

public enum BenefitType {
    TRANSPORT_VOUCHER("Vale Transporte"),
    MEAL_VOUCHER("Vale Refeição"),
    FOOD_VOUCHER("Vale Alimentação"),
    HEALTH_INSURANCE("Plano de Saúde"),
    DENTAL_INSURANCE("Plano Odontológico"),
    LIFE_INSURANCE("Seguro de Vida"),
    PENSION("Previdência Privada"),
    GYM("Academia/Bem-estar"),
    EDUCATION("Auxílio Educação"),
    CHILDCARE("Auxílio Creche"),
    FUEL("Vale Combustível"),
    PHARMACY("Vale Farmácia"),
    OPTICAL("Vale Ótica"),
    OTHER("Outros");

    private final String description;

    BenefitType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isMandatoryByDefault() {
        return this == TRANSPORT_VOUCHER;
    }
    public boolean isTaxableByDefault() {
        return this == FUEL;
    }
    public boolean requiresDependentByDefault() {
        return this == HEALTH_INSURANCE || this == DENTAL_INSURANCE ||
               this == LIFE_INSURANCE || this == CHILDCARE;
    }
}