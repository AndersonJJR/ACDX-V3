package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.IntegracaoFiscal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * DTOs for Fiscal Integration management API.
 */
public class IntegracaoFiscalDTO {

    @Schema(description = "Integration registration request")
    public record RegisterRequest(
            @NotBlank String companyId,
            @NotNull IntegrationType type,
            @NotBlank String name,
            @NotNull Map<String, Object> config,
            @NotBlank String environment,
            String endpointUrl,
            String certificateId
    ) {}

    @Schema(description = "Integration update request")
    public record UpdateRequest(
            String name,
            Map<String, Object> config,
            String environment,
            String endpointUrl,
            String certificateId,
            IntegrationStatus status
    ) {}

    @Schema(description = "Integration response")
    public record Response(
            String id,
            String companyId,
            IntegrationType type,
            String name,
            Map<String, Object> config,
            IntegrationStatus status,
            String environment,
            String endpointUrl,
            String certificateId,
            java.time.LocalDateTime lastSync,
            String lastSyncStatus,
            String lastError,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}

    @Schema(description = "Integration list response")
    public record ListResponse(
            List<Response> integrations,
            long total
    ) {}

    @Schema(description = "Integration test request")
    public record TestRequest(
            @NotBlank String integrationId,
            Map<String, Object> testPayload
    ) {}

    @Schema(description = "Integration test response")
    public record TestResponse(
            boolean success,
            String message,
            java.time.LocalDateTime testedAt,
            String requestPayload,
            String responsePayload
    ) {}

    public enum IntegrationType {
        SEFAZ_NFE, SEFAZ_NFCE, SEFAZ_CTE, SEFAZ_MDFE,
        MUNICIPIO_NFSE, ESOCIAL, EFD_REINF, DCTF_WEB,
        ECAC, GOV_BR, SIMBA
    }

    public enum IntegrationStatus {
        ACTIVE, INACTIVE, ERROR, CONFIGURING, TESTING
    }
}