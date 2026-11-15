package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dependent",
    indexes = {
        @Index(name = "idx_dependent_employee", columnList = "employee_id"),
        @Index(name = "idx_dependent_cpf", columnList = "cpf")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dependent extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Size(max = 14)
    @Column(name = "cpf", length = 14)
    private String cpf;

    @Size(max = 20)
    @Column(name = "rg", length = 20)
    private String rg;

    @NotNull
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private DependentRelationship relationship;

    @Column(name = "is_irrf_dependent")
    private Boolean isIrrfDependent = false;

    @Column(name = "is_health_insurance_dependent")
    private Boolean isHealthInsuranceDependent = false;

    @Column(name = "is_dental_insurance_dependent")
    private Boolean isDentalInsuranceDependent = false;

    @Column(name = "is_pension_dependent")
    private Boolean isPensionDependent = false;

    @Size(max = 100)
    @Column(name = "disability_certificate", length = 100)
    private String disabilityCertificate;

    @Size(max = 100)
    @Column(name = "disability_type", length = 100)
    private String disabilityType;

    @Column(name = "student_since")
    private LocalDate studentSince;

    @Column(name = "student_until")
    private LocalDate studentUntil;

    @Size(max = 255)
    @Column(name = "educational_institution", length = 255)
    private String educationalInstitution;

    public int getAge() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    public boolean isStudent() {
        return studentSince != null &&
               studentUntil != null &&
               !studentUntil.isBefore(LocalDate.now());
    }

    public boolean isTaxDependent() {
        return isIrrfDependent != null && isIrrfDependent;
    }

    public boolean isHealthDependent() {
        return isHealthInsuranceDependent != null && isHealthInsuranceDependent;
    }

    public String getMaskedCpf() {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "***.***" + cpf.substring(9);
    }
}