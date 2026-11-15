package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.Employee;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    @NotNull Long companyId,
    Long personId,
    @NotBlank @Size(max = 20) String employeeNumber,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate hireDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate terminationDate,
    Employee.EmployeeStatus status,
    Employee.ContractType contractType,
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$") String cpf,
    @Email @Size(max = 255) String email,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
    @Size(max = 20) String rg,
    Employee.Gender gender,
    Employee.MaritalStatus maritalStatus,
    String nationality,
    String naturalness,
    String motherName,
    String fatherName,
    String personalPhone,
    String corporatePhone,
    String addressCep,
    String addressStreet,
    String addressNumber,
    String addressComplement,
    String addressNeighborhood,
    String addressCity,
    String addressState,
    String addressCountry,
    String bankCode,
    String bankBranch,
    String bankAccount,
    String bankAccountType,
    String pixKey,
    String emergencyContactName,
    String emergencyContactPhone,
    String emergencyContactRelationship,
    String profilePhotoUrl
) {
    public static EmployeeDTO from(Employee employee) {
        return new EmployeeDTO(
            employee.uuid(), employee.companyId(), employee.personId(), employee.employeeNumber(),
            employee.hireDate(), employee.terminationDate(), employee.status(), employee.contractType(),
            employee.fullName(), employee.cpf(), employee.email(), employee.birthDate(),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }

    public Employee toDomain() {
        return new Employee(null, uuid, companyId, personId, employeeNumber, hireDate, terminationDate,
                          status, contractType, fullName, cpf, email, birthDate);
    }
}