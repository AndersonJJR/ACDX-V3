package com.erp.brazil.hr.domain.model;

public enum PayrollItemCategory {
    SALARY_BASE("Salário Base"),
    OVERTIME("Hora Extra"),
    NIGHT_SHIFT("Adicional Noturno"),
    HAZARDOUS("Insalubridade"),
    UNHEALTHY("Periculosidade"),
    VACATION("Férias"),
    THIRTEENTH("13º Salário"),
    NOTICE_PERIOD("Aviso Prévio"),
    FGTS("FGTS"),
    INSS_EMPLOYEE("INSS Empregado"),
    INSS_EMPLOYER("INSS Patronal"),
    IRRF("IRRF"),
    TRANSPORT_VOUCHER("Vale Transporte"),
    MEAL_VOUCHER("Vale Refeição/Alimentação"),
    HEALTH_INSURANCE("Plano de Saúde"),
    DENTAL_INSURANCE("Plano Odontológico"),
    LIFE_INSURANCE("Seguro de Vida"),
    UNION_DUES("Contribuição Sindical"),
    ADVANCE("Adiantamento"),
    LOAN("Empréstimo Consignado"),
    GARNISHMENT("Penhora Judicial"),
    TAX("Imposto"),
    OTHER("Outros");

    private final String description;

    PayrollItemCategory(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isTaxableInssByDefault() {
        return this == SALARY_BASE || this == OVERTIME || this == NIGHT_SHIFT ||
               this == HAZARDOUS || this == UNHEALTHY || this == VACATION ||
               this == THIRTEENTH || this == NOTICE_PERIOD;
    }
    public boolean isTaxableIrrfByDefault() {
        return isTaxableInssByDefault();
    }
    public boolean isTaxableFgtsByDefault() {
        return isTaxableInssByDefault();
    }
    public boolean isEmployerCostByDefault() {
        return this == INSS_EMPLOYER || this == FGTS;
    }
}