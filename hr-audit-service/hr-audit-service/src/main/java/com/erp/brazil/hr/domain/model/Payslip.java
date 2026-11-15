package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payslip",
    uniqueConstraints = @UniqueConstraint(name = "uk_payslip_payroll_employee", columnNames = {"payroll_id", "employee_id"}),
    indexes = {
        @Index(name = "idx_payslip_employee", columnList = "employee_id"),
        @Index(name = "idx_payslip_status", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @NotNull
    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @NotNull
    @Column(name = "inss_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal inssBase = BigDecimal.ZERO;

    @NotNull
    @Column(name = "inss_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal inssAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "irrf_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal irrfBase = BigDecimal.ZERO;

    @NotNull
    @Column(name = "irrf_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal irrfAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "fgts_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal fgtsBase = BigDecimal.ZERO;

    @NotNull
    @Column(name = "fgts_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fgtsAmount = BigDecimal.ZERO;

    @Column(name = "fgts_deposit_date")
    private LocalDate fgtsDepositDate;

    @Size(max = 500)
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "pdf_generated_at")
    private java.time.Instant pdfGeneratedAt;

    @Size(max = 500)
    @Column(name = "digital_signature", length = 500)
    private String digitalSignature;

    @Column(name = "signed_at")
    private java.time.Instant signedAt;

    @Column(name = "delivered_at")
    private java.time.Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 20)
    private DeliveryMethod deliveryMethod = DeliveryMethod.EMAIL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.GENERATED;

    @Size(max = 100)
    @Column(name = "access_token", length = 100)
    private String accessToken;

    @Column(name = "access_expires_at")
    private java.time.Instant accessExpiresAt;

    public BigDecimal getTotalDeductions() {
        return grossAmount.subtract(netAmount);
    }

    public BigDecimal getTotalEmployerCosts() {
        return inssAmount.add(fgtsAmount); // Simplified - would include other employer costs
    }

    public boolean isAccessible() {
        return accessToken != null &&
               accessExpiresAt != null &&
               accessExpiresAt.isAfter(java.time.Instant.now());
    }

    public void generateAccessToken() {
        this.accessToken = java.util.UUID.randomUUID().toString().replace("-", "");
        this.accessExpiresAt = java.time.Instant.now().plusSeconds(86400 * 30); // 30 days
    }
}