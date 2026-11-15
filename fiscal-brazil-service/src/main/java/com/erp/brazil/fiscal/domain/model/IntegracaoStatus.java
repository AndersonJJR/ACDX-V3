package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntegracaoStatus {
    ATIVA("Ativa"),
    INATIVA("Inativa"),
    ERRO("Erro"),
    MANUTENCAO("Manutenção"),
    TESTE("Teste");

    private final String description;
}