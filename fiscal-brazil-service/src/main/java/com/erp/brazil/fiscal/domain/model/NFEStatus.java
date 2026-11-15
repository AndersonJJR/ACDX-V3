package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFEStatus {
    PENDENTE("Pendente"),
    EM_PROCESSAMENTO("Em Processamento"),
    AUTORIZADA("Autorizada"),
    AUTORIZADA_COM_RESSALVA("Autorizada com Ressalva"),
    REJEITADA("Rejeitada"),
    CANCELADA("Cancelada"),
    INUTILIZADA("Inutilizada"),
    DENEGADA("Denegada"),
    ERRO("Erro"),
    CONTINGENCIA("Contingência");

    private final String description;
}