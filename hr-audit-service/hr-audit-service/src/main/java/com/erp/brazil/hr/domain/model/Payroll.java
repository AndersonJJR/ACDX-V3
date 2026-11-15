package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_payroll_company_period", columnNames = {"company_id", "reference_month", "reference_year"})
    },
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

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

    @Column(name = "total_employees")
    private int totalEmployees = 0;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalGrossAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalNetAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_inss_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInssAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_irrf_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIrrfAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_fgts_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFgtsAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_benefits_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBenefitsAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_deductions_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDeductionsAmount = BigDecimal.ZERO;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by")
    private Employee calculatedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Employee processedBy;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private Employee paidBy;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    public boolean isEditable() {
        return status == PayrollStatus.DRAFT || status == PayrollStatus.CALCULATING;
    }

    public boolean isFinalized() {
        return status == PayrollStatus.PROCESSED || status == PayrollStatus.PAID;
    }

    public enum PayrollStatus {
        DRAFT, CALCULATING, CALCULATED, APPROVED, PROCESSING, PROCESSED, PAID, CANCELLED, ERROR
    }
}