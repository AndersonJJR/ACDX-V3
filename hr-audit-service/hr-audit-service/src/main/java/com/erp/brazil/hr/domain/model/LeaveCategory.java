package com.erp.brazil.hr.domain.model;

public enum LeaveCategory {
    VACATION("Férias"),
    MEDICAL("Licença Médica"),
    MATERNITY("Licença Maternidade"),
    PATERNITY("Licença Paternidade"),
    MARRIAGE("Licença Casamento"),
    BEREAVEMENT("Licença Luto"),
    STUDY("Licença Estudo"),
    ELECTION("Mesário Eleitoral"),
    MILITARY("Serviço Militar"),
    BLOOD_DONATION("Doação de Sangue"),
    UNPAID("Licença Sem Remuneração"),
    COMPENSATORY("Banco de Horas"),
    OTHER("Outros");

    private final String description;

    LeaveCategory(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isPaidByDefault() {
        return this != UNPAID && this != STUDY;
    }
    public boolean requiresDocumentByDefault() {
        return this == MEDICAL || this == MATERNITY || this == PATERNITY ||
               this == MARRIAGE || this == BEREAVEMENT || this == ELECTION ||
               this == MILITARY || this == BLOOD_DONATION;
    }
}