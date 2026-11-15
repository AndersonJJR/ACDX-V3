package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_cpf", columnNames = "cpf"),
        @UniqueConstraint(name = "uk_employee_number_company", columnNames = {"employee_number", "company_id"})
    },
    indexes = {
        @Index(name = "idx_employee_company", columnList = "company_id"),
        @Index(name = "idx_employee_status", columnList = "status"),
        @Index(name = "idx_employee_hire_date", columnList = "hire_date")
    })
@SQLDelete(sql = "UPDATE employee SET status = 'TERMINATED', termination_date = CURRENT_DATE WHERE id = ?")
@FilterDef(name = "activeEmployeeFilter", parameters = @ParamDef(name = "status", type = String.class))
@Filter(name = "activeEmployeeFilter", condition = "status != 'TERMINATED'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "employee_number", nullable = false, length = 20)
    private String employeeNumber;

    @NotBlank
    @Size(max = 255)
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @NotBlank
    @Size(max = 14)
    @Column(name = "cpf", nullable = false, length = 14)
    private String cpf;

    @Size(max = 20)
    @Column(name = "rg", length = 20)
    private String rg;

    @NotNull
    @Past
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1)
    private Gender gender;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false, length = 20)
    private MaritalStatus maritalStatus;

    @Size(max = 50)
    @Column(name = "nationality", length = 50)
    private String nationality = "BRASILEIRA";

    @Size(max = 100)
    @Column(name = "naturalness", length = 100)
    private String naturalness;

    @Size(max = 255)
    @Column(name = "mother_name", length = 255)
    private String motherName;

    @Size(max = 255)
    @Column(name = "father_name", length = 255)
    private String fatherName;

    @Email
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Size(max = 20)
    @Column(name = "personal_phone", length = 20)
    private String personalPhone;

    @Size(max = 20)
    @Column(name = "corporate_phone", length = 20)
    private String corporatePhone;

    @Size(max = 10)
    @Column(name = "address_cep", length = 10)
    private String addressCep;

    @Size(max = 255)
    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Size(max = 20)
    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Size(max = 100)
    @Column(name = "address_complement", length = 100)
    private String addressComplement;

    @Size(max = 100)
    @Column(name = "address_neighborhood", length = 100)
    private String addressNeighborhood;

    @Size(max = 100)
    @Column(name = "address_city", length = 100)
    private String addressCity;

    @Size(max = 2)
    @Column(name = "address_state", length = 2)
    private String addressState;

    @Size(max = 50)
    @Column(name = "address_country", length = 50)
    private String addressCountry = "BRASIL";

    @Size(max = 3)
    @Column(name = "bank_code", length = 3)
    private String bankCode;

    @Size(max = 10)
    @Column(name = "bank_branch", length = 10)
    private String bankBranch;

    @Size(max = 20)
    @Column(name = "bank_account", length = 20)
    private String bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_account_type", length = 10)
    private BankAccountType bankAccountType;

    @Size(max = 100)
    @Column(name = "pix_key", length = 100)
    private String pixKey;

    @Size(max = 255)
    @Column(name = "emergency_contact_name", length = 255)
    private String emergencyContactName;

    @Size(max = 20)
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Size(max = 50)
    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    @Size(max = 500)
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Size(max = 500)
    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    // Relationships
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EmploymentContract employmentContract;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimeRecord> timeRecords = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TimeSheet> timeSheets = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeaveRequest> leaveRequests = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeaveBalance> leaveBalances = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PayrollItem> payrollItems = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payslip> payslips = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeBenefit> employeeBenefits = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Dependent> dependents = new ArrayList<>();

    public int getAge() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    public int getTenureYears() {
        LocalDate endDate = terminationDate != null ? terminationDate : LocalDate.now();
        return java.time.Period.between(hireDate, endDate).getYears();
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }

    public String getMaskedCpf() {
        if (cpf == null || cpf.length() != 14) return cpf;
        return "***.***.***-**";
    }

    public String getMaskedEmail() {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String masked = parts[0].length() > 2 ?
            parts[0].substring(0, 2) + "***" : "***";
        return masked + "@" + parts[1];
    }

    public String getMaskedPhone() {
        if (personalPhone == null || personalPhone.length() < 4) return personalPhone;
        return "***" + personalPhone.substring(personalPhone.length() - 4);
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE, TERMINATED, SUSPENDED
    }

    public enum Gender {
        M, F, O
    }

    public enum MaritalStatus {
        SINGLE, MARRIED, DIVORCED, WIDOWED, STABLE_UNION
    }

    public enum BankAccountType {
        CHECKING, SAVINGS
    }
}