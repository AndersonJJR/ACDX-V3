package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dependent",
    uniqueConstraints = @UniqueConstraint(name = "uk_dependent_uuid", columnNames = "uuid"),
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
    private Relationship relationship;

    @Column(name = "is_irrf_dependent")
    private boolean isIrrfDependent = false;

    @Column(name = "is_health_insurance_dependent")
    private boolean isHealthInsuranceDependent = false;

    @Column(name = "is_dental_insurance_dependent")
    private boolean isDentalInsuranceDependent = false;

    @Column(name = "is_pension_dependent")
    private boolean isPensionDependent = false;

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

    public boolean isMinor() {
        return getAge() < 18;
    }

    public boolean isStudent() {
        return studentSince != null && (studentUntil == null || !LocalDate.now().isAfter(studentUntil));
    }

    public String getMaskedCpf() {
        if (cpf == null || cpf.length() != 14) return "***";
        return "***.***.***-**";
    }

    public enum Gender {
        M, F, O
    }

    public enum Relationship {
        SPOUSE, CHILD, STEPCHILD, PARENT, GRANDPARENT, SIBLING, OTHER
    }
}