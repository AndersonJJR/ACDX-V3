package com.erp.brazil.hr.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Payslip(
    Long id,
    UUID uuid,
    Long payrollId,
    Long employeeId,
    BigDecimal grossAmount,
    BigDecimal netAmount,
    BigDecimal inssBase,
    BigDecimal inssAmount,
    BigDecimal irrfBase,
    BigDecimal irrfAmount,
    BigDecimal fgtsBase,
    BigDecimal fgtsAmount,
    LocalDate fgtsDepositDate,
    String pdfPath,
    LocalDateTime pdfGeneratedAt,
    String digitalSignature,
    LocalDateTime signedAt,
    LocalDateTime deliveredAt,
    DeliveryMethod deliveryMethod,
    PayslipStatus status,
    String accessToken,
    LocalDateTime accessExpiresAt
) {
    public enum DeliveryMethod {
        EMAIL, PORTAL, PRINTED, WHATSAPP
    }

    public enum PayslipStatus {
        GENERATED, SIGNED, DELIVERED, VIEWED, DOWNLOADED
    }

    public boolean isAccessible() {
        return accessExpiresAt == null || accessExpiresAt.isAfter(LocalDateTime.now());
    }

    public BigDecimal totalDeductions() {
        return inssAmount.add(irrfAmount);
    }

    public BigDecimal totalEmployerCosts() {
        return fgtsAmount;
    }
}