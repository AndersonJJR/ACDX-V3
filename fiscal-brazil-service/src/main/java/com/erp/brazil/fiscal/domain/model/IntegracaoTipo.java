package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntegracaoTipo {
    NFE_EMISSION("NFE_EMISSION", "NF-e Emission"),
    NFE_QUERY("NFE_QUERY", "NF-e Query"),
    NFE_CANCELLATION("NFE_CANCELLATION", "NF-e Cancellation"),
    NFE_INUTILIZATION("NFE_INUTILIZATION", "NF-e Inutilization"),
    NFE_CORRECTION_LETTER("NFE_CORRECTION_LETTER", "NF-e Correction Letter"),
    NFE_RECIPIENT_MANIFESTATION("NFE_RECIPIENT_MANIFESTATION", "NF-e Recipient Manifestation"),
    NFSE_EMISSION("NFSE_EMISSION", "NFS-e Emission"),
    NFSE_QUERY("NFSE_QUERY", "NFS-e Query"),
    NFSE_CANCELLATION("NFSE_CANCELLATION", "NFS-e Cancellation"),
    CTE_EMISSION("CTE_EMISSION", "CT-e Emission"),
    CTE_QUERY("CTE_QUERY", "CT-e Query"),
    CTE_CANCELLATION("CTE_CANCELLATION", "CT-e Cancellation"),
    SPED_TRANSMISSION("SPED_TRANSMISSION", "SPED Transmission"),
    LEIS_SYNC("LEIS_SYNC", "Legislation Sync (NCM/CEST/CFOP)");

    private final String code;
    private final String description;
}