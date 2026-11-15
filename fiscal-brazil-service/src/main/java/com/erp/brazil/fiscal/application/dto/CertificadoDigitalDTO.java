package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.CertificateStatus;
import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.model.IntegracaoFiscal;
import com.erp.brazil.fiscal.domain.model.IntegracaoTipo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Digital Certificate A1 management API.
 */
public class CertificadoDigitalDTO {

    @Schema(description = "Certificate registration request")
    public record RegisterRequest(
            @NotBlank String companyId,
            @NotBlank String alias,
            @NotBlank String pfxBase64,
            @NotBlank String password,
            @NotBlank String cnpj,
            @NotBlank String companyName,
            int renewalAlertDays
    ) {}

    @Schema(description = "Certificate registration response")
    public record RegisterResponse(
            String certificateId,
            String alias,
            String cnpj,
            String companyName,
            LocalDate expiryDate,
            CertificateStatus status,
            int daysUntilExpiry,
            java.time.LocalDateTime registeredAt
    ) {}

    @Schema(description = "Certificate update request")
    public record UpdateRequest(
            String alias,
            String pfxBase64,
            String password,
            int renewalAlertDays
    ) {}

    @Schema(description = "Certificate response")
    public record Response(
            String id,
            String companyId,
            String alias,
            String cnpj,
            String companyName,
            LocalDate expiryDate,
            CertificateStatus status,
            int daysUntilExpiry,
            String serialNumber,
            String issuer,
            String subject,
            String thumbprint,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt,
            java.time.LocalDateTime lastUsedAt,
            int renewalAlertDays
    ) {}

    @Schema(description = "Certificate list response")
    public record ListResponse(
            List<Response> certificates,
            int activeCount,
            int expiringSoonCount,
            int expiredCount
    ) {}

    @Schema(description = "Certificate renewal alert request")
    public record RenewalAlertRequest(
            @NotBlank String companyId,
            int alertDays
    ) {}

    @Schema(description = "Certificate test request")
    public record TestRequest(
            @NotBlank String certificateId,
            @NotNull IntegracaoTipo type
    ) {}

    @Schema(description = "Certificate test response")
    public record TestResponse(
            boolean success,
            String message,
            java.time.LocalDateTime testedAt,
            String endpointUrl
    ) {}
}