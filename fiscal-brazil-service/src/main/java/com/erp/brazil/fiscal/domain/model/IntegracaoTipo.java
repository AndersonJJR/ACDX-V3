package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntegracaoTipo {
    NFE_EMISSAO("NFE_EMISSAO", "Emissão NF-e"),
    NFE_CONSULTA("NFE_CONSULTA", "Consulta NF-e"),
    NFE_CANCELAMENTO("NFE_CANCELAMENTO", "Cancelamento NF-e"),
    NFE_INUTILIZACAO("NFE_INUTILIZACAO", "Inutilização NF-e"),
    NFE_CARTA_CORRECAO("NFE_CARTA_CORRECAO", "Carta de Correção NF-e"),
    NFE_MANIFESTACAO("NFE_MANIFESTACAO", "Manifestação do Destinatário"),
    NFSE_EMISSAO("NFSE_EMISSAO", "Emissão NFS-e"),
    NFSE_CONSULTA("NFSE_CONSULTA", "Consulta NFS-e"),
    NFSE_CANCELAMENTO("NFSE_CANCELAMENTO", "Cancelamento NFS-e"),
    CTE_EMISSAO("CTE_EMISSAO", "Emissão CT-e"),
    CTE_CONSULTA("CTE_CONSULTA", "Consulta CT-e"),
    CTE_CANCELAMENTO("CTE_CANCELAMENTO", "Cancelamento CT-e"),
    SPED_TRANSMISSAO("SPED_TRANSMISSAO", "Transmissão SPED"),
    LEIS_SYNC("LEIS_SYNC", "Sincronização de Leis (NCM/CEST/CFOP)");

    private final String code;
    private final String description;
}