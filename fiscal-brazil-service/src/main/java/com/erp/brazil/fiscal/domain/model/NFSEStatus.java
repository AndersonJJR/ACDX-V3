package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFSEStatus {
    PENDENTE("Pendente"),
    EM_PROCESSAMENTO("Em Processamento"),
    AUTORIZADA("Autorizada"),
    REJEITADA("Rejeitada"),
    CANCELADA("Cancelada"),
    SUBSTITUIDA("Substituída"),
    ERRO("Erro");

    private final String description;
}