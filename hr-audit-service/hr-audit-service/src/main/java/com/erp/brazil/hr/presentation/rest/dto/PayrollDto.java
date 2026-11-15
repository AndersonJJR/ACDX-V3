package com.erp.brazil.hr.presentation.rest.dto;

import com.erp.brazil.hr.domain.model.PayrollItemCategory;
import com.erp.brazil.hr.domain.model.PayrollItemType;
import com.erp.brazil.hr.domain.model.PayrollStatus;
import com.erp.brazil.hr.domain.model.PayslipStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PayrollDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollRequest {
        private Integer referenceMonth;
        private Integer referenceYear;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollResponse {
        private Long id;
        private String uuid;
        private Long companyId;
        private Integer referenceMonth;
        private Integer referenceYear;
        private String referencePeriod;
        private PayrollStatus status;
        private String statusDescription;
        private Integer totalEmployees;
        private BigDecimal totalGrossAmount;
        private BigDecimal totalNetAmount;
        private BigDecimal totalInssAmount;
        private BigDecimal totalIrrfAmount;
        private BigDecimal totalFgtsAmount;
        private BigDecimal totalBenefitsAmount;
        private BigDecimal totalDeductionsAmount;
        private java.time.Instant calculatedAt;
        private java.time.Instant approvedAt;
        private java.time.Instant processedAt;
        private java.time.Instant paidAt;
        private String errorMessage;
        private String observations;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollItemResponse {
        private Long id;
        private String uuid;
        private Long payrollId;
        private Long employeeId;
        private String employeeName;
        private PayrollItemType itemType;
        private String itemTypeDescription;
        private PayrollItemCategory category;
        private String categoryDescription;
        private String code;
        private String description;
        private BigDecimal referenceValue;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private Boolean taxableInss;
        private Boolean taxableIrrf;
        private Boolean taxableFgts;
        private Boolean isFixed;
        private String calculationBasis;
        private Integer displayOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayslipResponse {
        private Long id;
        private String uuid;
        private Long payrollId;
        private Long employeeId;
        private String employeeName;
        private String employeeNumber;
        private BigDecimal grossAmount;
        private BigDecimal netAmount;
        private BigDecimal inssBase;
        private BigDecimal inssAmount;
        private BigDecimal irrfBase;
        private BigDecimal irrfAmount;
        private BigDecimal fgtsBase;
        private BigDecimal fgtsAmount;
        private LocalDate fgtsDepositDate;
        private String pdfPath;
        private java.time.Instant pdfGeneratedAt;
        private String digitalSignature;
        private java.time.Instant signedAt;
        private java.time.Instant deliveredAt;
        private String deliveryMethod;
        private PayslipStatus status;
        private String statusDescription;
        private String accessToken;
        private java.time.Instant accessExpiresAt;
        private String downloadUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeePayslipDetail {
        private PayslipResponse payslip;
        private List<PayrollItemResponse> earnings;
        private List<PayrollItemResponse> deductions;
        private List<PayrollItemResponse> employerCosts;
        private BigDecimal totalEarnings;
        private BigDecimal totalDeductions;
        private BigDecimal totalEmployerCosts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<PayrollResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollSummary {
        private Integer totalEmployees;
        private BigDecimal totalGross;
        private BigDecimal totalNet;
        private BigDecimal totalInss;
        private BigDecimal totalIrrf;
        private BigDecimal totalFgts;
        private BigDecimal totalBenefits;
        private BigDecimal totalDeductions;
        private Map<String, BigDecimal> byCategory;
    }
}