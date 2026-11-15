package com.erp.brazil.hr.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record Dependent(
    Long id,
    UUID uuid,
    Long employeeId,
    String fullName,
    String cpf,
    String rg,
    LocalDate birthDate,
    Gender gender,
    Relationship relationship,
    boolean isIrrfDependent,
    boolean isHealthInsuranceDependent,
    boolean isDentalInsuranceDependent,
    boolean isPensionDependent,
    String disabilityCertificate,
    String disabilityType,
    LocalDate studentSince,
    LocalDate studentUntil,
    String educationalInstitution
) {
    public enum Gender {
        M, F, O
    }

    public enum Relationship {
        SPOUSE, CHILD, STEPCHILD, PARENT, GRANDPARENT, SIBLING, OTHER
    }

    public int age() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    public boolean isMinor() {
        return age() < 18;
    }

    public boolean isStudent() {
        return studentSince != null && (studentUntil == null || !LocalDate.now().isAfter(studentUntil));
    }

    public String maskedCpf() {
        if (cpf == null || cpf.length() != 14) return "***";
        return String.format("***.***.%s-%s", cpf.substring(6, 9), cpf.substring(9, 11));
    }
}