package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.Dependent;
import lombok.*;

import java.time.LocalDate;

public class DependentDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String fullName;
        private String cpf;
        private String rg;
        private LocalDate birthDate;
        private Dependent.Gender gender;
        private Dependent.Relationship relationship;
        private Boolean isIrrfDependent;
        private Boolean isHealthInsuranceDependent;
        private Boolean isDentalInsuranceDependent;
        private Boolean isPensionDependent;
        private String disabilityCertificate;
        private String disabilityType;
        private LocalDate studentSince;
        private LocalDate studentUntil;
        private String educationalInstitution;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String uuid;
        private Long employeeId;
        private String fullName;
        private String cpf;
        private String maskedCpf;
        private String rg;
        private LocalDate birthDate;
        private Dependent.Gender gender;
        private Dependent.Relationship relationship;
        private Boolean isIrrfDependent;
        private Boolean isHealthInsuranceDependent;
        private Boolean isDentalInsuranceDependent;
        private Boolean isPensionDependent;
        private String disabilityCertificate;
        private String disabilityType;
        private LocalDate studentSince;
        private LocalDate studentUntil;
        private String educationalInstitution;
        private int age;
        private boolean isMinor;
        private boolean isStudent;
    }
}