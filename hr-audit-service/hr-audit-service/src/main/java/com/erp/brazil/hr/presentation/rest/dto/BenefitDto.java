package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.Benefit;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BenefitDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String code;
        private String name;
        private String description;
        private Benefit.BenefitType benefitType;
        private String providerName;
        private String providerCnpj;
        private String planName;
        private String planNumber;
        private BigDecimal employeeContributionPct;
        private BigDecimal employerContributionPct;
        private BigDecimal maxEmployeeValue;
        private BigDecimal maxEmployerValue;
        private Boolean isMandatory;
        private Boolean isTaxable;
        private Boolean requiresDependent;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String uuid;
        private Long companyId;
        private String code;
        private String name;
        private String description;
        private Benefit.BenefitType benefitType;
        private String providerName;
        private String providerCnpj;
        private String planName;
        private String planNumber;
        private BigDecimal employeeContributionPct;
        private BigDecimal employerContributionPct;
        private BigDecimal maxEmployeeValue;
        private BigDecimal maxEmployerValue;
        private Boolean isMandatory;
        private Boolean isTaxable;
        private Boolean requiresDependent;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isActive;
    }
}