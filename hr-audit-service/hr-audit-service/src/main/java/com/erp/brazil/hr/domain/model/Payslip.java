package com.erp.brazil.hr.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payslip",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payslip_uuid", columnNames = "uuid"),
        @UniqueConstraint(name = "uk_payslip_payroll_employee", columnNames = {"payroll_id", "employee_id"})
    },
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
    @DecimalMin("0.00")
    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "inss_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal inssBase = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "inss_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal inssAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "irrf_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal irrfBase = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "irrf_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal irrfAmount = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "fgts_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal fgtsBase = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "fgts_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fgtsAmount = BigDecimal.ZERO;

    @Column(name = "fgts_deposit_date")
    private LocalDate fgtsDepositDate;

    @Size(max = 500)
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "pdf_generated_at")
    private LocalDateTime pdfGeneratedAt;

    @Size(max = 500)
    @Column(name = "digital_signature", length = 500)
    private String digitalSignature;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", length = 10)
    private DeliveryMethod deliveryMethod = DeliveryMethod.EMAIL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.GENERATED;

    @Size(max = 100)
    @Column(name = "access_token", length = 100)
    private String accessToken;

    @Column(name = "access_expires_at")
    private LocalDateTime accessExpiresAt;

    public boolean isAccessible() {
        return accessExpiresAt == null || accessExpiresAt.isAfter(LocalDateTime.now());
    }

    public BigDecimal getTotalDeductions() {
        return inssAmount.add(irrfAmount);
    }

    public BigDecimal getTotalEmployerCosts() {
        return fgtsAmount;
    }

    public enum DeliveryMethod {
        EMAIL, PORTAL, PRINTED, WHATSAPP
    }

    public enum PayslipStatus {
        GENERATED, SIGNED, DELIVERED, VIEWED, DOWNLOADED
    }
}