package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CTEStatus {
    PENDENTE("Pendente"),
    EM_PROCESSAMENTO("Em Processamento"),
    AUTORIZADA("Autorizada"),
    AUTORIZADA_COM_RESSALVA("Autorizada com Ressalva"),
    REJEITADA("Rejeitada"),
    CANCELADA("Cancelada"),
    INUTILIZADA("Inutilizada"),
    ERRO("Erro"),
    CONTINGENCIA("Contingência");

    private final String description;
}