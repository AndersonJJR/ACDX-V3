package com.erp.brazil.hr.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record Employee(
    Long id,
    UUID uuid,
    Long companyId,
    Long personId,
    String employeeNumber,
    LocalDate hireDate,
    LocalDate terminationDate,
    EmployeeStatus status,
    ContractType contractType,
    String fullName,
    String cpf,
    String email,
    LocalDate birthDate
) {
    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE, TERMINATED, SUSPENDED
    }

    public enum ContractType {
        CLT_INDEFINITE, CLT_FIXED_TERM, CLT_TEMPORARY, APPRENTICE, INTERNSHIP, STATUTORY, COOPERATIVE
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    public String maskedCpf() {
        if (cpf == null || cpf.length() != 14) return "***";
        return String.format("***.***.%s-%s", cpf.substring(6, 9), cpf.substring(9, 11));
    }
}