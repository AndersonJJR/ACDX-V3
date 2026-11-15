package com.erp.brazil.hr.application.dto;

import com.erp.brazil.hr.domain.model.Payslip;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayslipDTO(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY) UUID uuid,
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime pdfGeneratedAt,
    String digitalSignature,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime signedAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime deliveredAt,
    Payslip.DeliveryMethod deliveryMethod,
    Payslip.PayslipStatus status,
    String accessToken,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime accessExpiresAt
) {
    public static PayslipDTO from(Payslip payslip) {
        return new PayslipDTO(
            payslip.uuid(), payslip.payrollId(), payslip.employeeId(), payslip.grossAmount(), payslip.netAmount(),
            payslip.inssBase(), payslip.inssAmount(), payslip.irrfBase(), payslip.irrfAmount(),
            payslip.fgtsBase(), payslip.fgtsAmount(), payslip.fgtsDepositDate(), payslip.pdfPath(),
            payslip.pdfGeneratedAt(), payslip.digitalSignature(), payslip.signedAt(), payslip.deliveredAt(),
            payslip.deliveryMethod(), payslip.status(), payslip.accessToken(), payslip.accessExpiresAt()
        );
    }

    public BigDecimal totalDeductions() {
        return inssAmount.add(irrfAmount);
    }
}