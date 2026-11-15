package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Payroll(
    Long id,
    UUID uuid,
    Long companyId,
    int referenceMonth,
    int referenceYear,
    PayrollStatus status,
    int totalEmployees,
    BigDecimal totalGrossAmount,
    BigDecimal totalNetAmount,
    BigDecimal totalInssAmount,
    BigDecimal totalIrrfAmount,
    BigDecimal totalFgtsAmount,
    BigDecimal totalBenefitsAmount,
    BigDecimal totalDeductionsAmount,
    LocalDateTime calculatedAt,
    Long calculatedBy,
    LocalDateTime approvedAt,
    Long approvedBy,
    LocalDateTime processedAt,
    Long processedBy,
    LocalDateTime paidAt,
    Long paidBy,
    String errorMessage,
    String observations
) {
    public enum PayrollStatus {
        DRAFT, CALCULATING, CALCULATED, APPROVED, PROCESSING, PROCESSED, PAID, CANCELLED, ERROR
    }

    public boolean isEditable() {
        return status == PayrollStatus.DRAFT || status == PayrollStatus.CALCULATING;
    }

    public boolean isFinalized() {
        return status == PayrollStatus.PROCESSED || status == PayrollStatus.PAID;
    }
}