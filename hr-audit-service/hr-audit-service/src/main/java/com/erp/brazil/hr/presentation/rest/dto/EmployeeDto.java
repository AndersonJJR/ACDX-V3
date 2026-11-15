package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EmployeeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String employeeNumber;
        private String fullName;
        private String cpf;
        private String rg;
        private LocalDate birthDate;
        private Gender gender;
        private MaritalStatus maritalStatus;
        private String nationality;
        private String naturalness;
        private String motherName;
        private String fatherName;
        private String email;
        private String personalPhone;
        private String corporatePhone;
        private String addressCep;
        private String addressStreet;
        private String addressNumber;
        private String addressComplement;
        private String addressNeighborhood;
        private String addressCity;
        private String addressState;
        private String addressCountry;
        private String bankCode;
        private String bankBranch;
        private String bankAccount;
        private BankAccountType bankAccountType;
        private String pixKey;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String emergencyContactRelationship;
        private LocalDate hireDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String uuid;
        private String employeeNumber;
        private String fullName;
        private String cpf;
        private String maskedCpf;
        private String email;
        private String maskedEmail;
        private LocalDate birthDate;
        private Gender gender;
        private MaritalStatus maritalStatus;
        private EmployeeStatus status;
        private LocalDate hireDate;
        private LocalDate terminationDate;
        private String positionTitle;
        private String departmentName;
        private BigDecimal salaryBase;
        private ContractType contractType;
        private WorkLocation workLocation;
        private int age;
        private int tenureYears;
        private boolean active;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractRequest {
        private ContractType contractType;
        private WorkJourney workJourney;
        private BigDecimal weeklyHours;
        private BigDecimal dailyHours;
        private BigDecimal salaryBase;
        private String positionTitle;
        private String positionCbo;
        private Long departmentId;
        private Long costCenterId;
        private Long reportingToId;
        private WorkLocation workLocation;
        private String workCity;
        private String workState;
        private Long unionId;
        private Long collectiveAgreementId;
        private LocalDate probationEndDate;
        private LocalDate contractStartDate;
        private LocalDate contractEndDate;
        private Boolean fgtsOptIn;
        private String fgtsAccount;
        private Boolean benefitTransportVoucher;
        private Boolean benefitMealVoucher;
        private Boolean benefitHealthInsurance;
        private Boolean benefitDentalInsurance;
        private Boolean benefitLifeInsurance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractResponse {
        private Long id;
        private String uuid;
        private ContractType contractType;
        private WorkJourney workJourney;
        private BigDecimal weeklyHours;
        private BigDecimal dailyHours;
        private BigDecimal salaryBase;
        private BigDecimal salaryHourly;
        private String positionTitle;
        private String positionCbo;
        private Long departmentId;
        private String departmentName;
        private Long costCenterId;
        private String costCenterName;
        private Long reportingToId;
        private String reportingToName;
        private WorkLocation workLocation;
        private String workCity;
        private String workState;
        private Long unionId;
        private Long collectiveAgreementId;
        private LocalDate probationEndDate;
        private LocalDate contractStartDate;
        private LocalDate contractEndDate;
        private Integer renewalCount;
        private Boolean fgtsOptIn;
        private String fgtsAccount;
        private Boolean benefitTransportVoucher;
        private Boolean benefitMealVoucher;
        private Boolean benefitHealthInsurance;
        private Boolean benefitDentalInsurance;
        private Boolean benefitLifeInsurance;
        private boolean active;
        private boolean onProbation;
        private int noticePeriodDays;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<Response> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}