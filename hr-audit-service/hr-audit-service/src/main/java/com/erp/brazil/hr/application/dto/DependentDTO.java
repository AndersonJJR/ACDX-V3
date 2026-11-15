package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.Dependent;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record DependentDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    @NotNull Long employeeId,
    @NotBlank @Size(max = 255) String fullName,
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$") String cpf,
    String rg,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
    @NotNull Dependent.Gender gender,
    @NotNull Dependent.Relationship relationship,
    boolean isIrrfDependent,
    boolean isHealthInsuranceDependent,
    boolean isDentalInsuranceDependent,
    boolean isPensionDependent,
    String disabilityCertificate,
    String disabilityType,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate studentSince,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate studentUntil,
    String educationalInstitution
) {
    public static DependentDTO from(Dependent dependent) {
        return new DependentDTO(
            dependent.uuid(), dependent.employeeId(), dependent.fullName(), dependent.cpf(), dependent.rg(),
            dependent.birthDate(), dependent.gender(), dependent.relationship(), dependent.isIrrfDependent(),
            dependent.isHealthInsuranceDependent(), dependent.isDentalInsuranceDependent(), dependent.isPensionDependent(),
            dependent.disabilityCertificate(), dependent.disabilityType(), dependent.studentSince(),
            dependent.studentUntil(), dependent.educationalInstitution()
        );
    }

    public int age() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
}