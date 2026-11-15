package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CertificadoStatus {
    ATIVO("Ativo"),
    EXPIRADO("Expirado"),
    REVOGADO("Revogado"),
    PENDENTE_RENOVACAO("Pendente de Renovação"),
    ERRO("Erro");

    private final String description;
}