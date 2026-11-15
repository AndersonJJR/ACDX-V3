package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payroll",
    uniqueConstraints = @UniqueConstraint(name = "uk_payroll_company_period", columnNames = {"company_id", "reference_month", "reference_year"}),
    indexes = {
        @Index(name = "idx_payroll_status", columnList = "status"),
        @Index(name = "idx_payroll_period", columnList = "reference_year,reference_month")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll extends BaseEntity {

    @NotNull
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "reference_month", nullable = false)
    private Integer referenceMonth;

    @NotNull
    @Min(2000)
    @Max(2100)
    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @NotNull
    @Column(name = "total_employees", nullable = false)
    private Integer totalEmployees = 0;

    @NotNull
    @Column(name = "total_gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalGrossAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalNetAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_inss_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInssAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_irrf_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIrrfAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_fgts_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFgtsAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_benefits_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBenefitsAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_deductions_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductionsAmount = BigDecimal.ZERO;

    @Column(name = "calculated_at")
    private java.time.Instant calculatedAt;

    @Column(name = "calculated_by")
    private Long calculatedBy;

    @Column(name = "approved_at")
    private java.time.Instant approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "processed_at")
    private java.time.Instant processedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "paid_at")
    private java.time.Instant paidAt;

    @Column(name = "paid_by")
    private Long paidBy;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    public String getReferencePeriod() {
        return String.format("%02d/%d", referenceMonth, referenceYear);
    }

    public boolean isEditable() {
        return status.isEditable();
    }

    public boolean isFinal() {
        return status.isFinal();
    }

    public boolean canTransitionTo(PayrollStatus newStatus) {
        return status.canTransitionTo(newStatus);
    }

    public void transitionTo(PayrollStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", status, newStatus));
        }
        this.status = newStatus;
    }
}