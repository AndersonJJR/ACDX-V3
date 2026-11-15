package com.erp.brazil.hr.domain.model;

public enum TimeRecordType {
    ENTRY("Entrada"),
    EXIT_LUNCH("Saída Almoço"),
    RETURN_LUNCH("Retorno Almoço"),
    EXIT("Saída"),
    OVERTIME_ENTRY("Entrada Hora Extra"),
    OVERTIME_EXIT("Saída Hora Extra"),
    ABSENCE("Falta"),
    MEDICAL_LEAVE("Licença Médica"),
    JUSTIFIED_ABSENCE("Falta Justificada");

    private final String description;

    TimeRecordType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public boolean isWorkRecord() {
        return this == ENTRY || this == EXIT_LUNCH || this == RETURN_LUNCH || this == EXIT;
    }
    public boolean isOvertimeRecord() {
        return this == OVERTIME_ENTRY || this == OVERTIME_EXIT;
    }
    public boolean isAbsenceRecord() {
        return this == ABSENCE || this == MEDICAL_LEAVE || this == JUSTIFIED_ABSENCE;
    }
}