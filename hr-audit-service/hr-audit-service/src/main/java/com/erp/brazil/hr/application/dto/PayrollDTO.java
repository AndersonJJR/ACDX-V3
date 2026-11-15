package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.Payroll;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
    Long companyId,
    int referenceMonth,
    int referenceYear,
    Payroll.PayrollStatus status,
    int totalEmployees,
    BigDecimal totalGrossAmount,
    BigDecimal totalNetAmount,
    BigDecimal totalInssAmount,
    BigDecimal totalIrrfAmount,
    BigDecimal totalFgtsAmount,
    BigDecimal totalBenefitsAmount,
    BigDecimal totalDeductionsAmount,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime calculatedAt,
    Long calculatedBy,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime approvedAt,
    Long approvedBy,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime processedAt,
    Long processedBy,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime paidAt,
    Long paidBy,
    String errorMessage,
    String observations
) {
    public static PayrollDTO from(Payroll payroll) {
        return new PayrollDTO(
            payroll.uuid(), payroll.companyId(), payroll.referenceMonth(), payroll.referenceYear(),
            payroll.status(), payroll.totalEmployees(), payroll.totalGrossAmount(), payroll.totalNetAmount(),
            payroll.totalInssAmount(), payroll.totalIrrfAmount(), payroll.totalFgtsAmount(),
            payroll.totalBenefitsAmount(), payroll.totalDeductionsAmount(), payroll.calculatedAt(),
            payroll.calculatedBy(), payroll.approvedAt(), payroll.approvedBy(), payroll.processedAt(),
            payroll.processedBy(), payroll.paidAt(), payroll.paidBy(), payroll.errorMessage(),
            payroll.observations()
        );
    }
}