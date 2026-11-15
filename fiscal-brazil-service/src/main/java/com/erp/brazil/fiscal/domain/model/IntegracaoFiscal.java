package com.erp.brazil.fiscal.domain.model;

import java.time.LocalDateTime;

/**
 * Fiscal integration configuration for external systems (SEFAZ, municipalities, etc.).
 */
public record IntegracaoFiscal(
        String id,
        String companyId,
        IntegrationType type,
        String name,
        String configJson,
        IntegrationStatus status,
        String environment,
        String endpointUrl,
        String certificateId,
        LocalDateTime lastSync,
        String lastSyncStatus,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public enum IntegrationType {
        SEFAZ_NFE, SEFAZ_NFCE, SEFAZ_CTE, SEFAZ_MDFE,
        MUNICIPIO_NFSE, ESOCIAL, EFD_REINF, DCTF_WEB,
        ECAC, GOV_BR, SIMBA
    }

    public enum IntegrationStatus {
        ACTIVE, INACTIVE, ERROR, CONFIGURING, TESTING
    }

    public static IntegracaoFiscal create(String companyId, IntegrationType type, String name,
                                           String configJson, String environment,
                                           String endpointUrl, String certificateId) {
        return new IntegracaoFiscal(
                java.util.UUID.randomUUID().toString(),
                companyId,
                type,
                name,
                configJson,
                IntegrationStatus.CONFIGURING,
                environment,
                endpointUrl,
                certificateId,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}