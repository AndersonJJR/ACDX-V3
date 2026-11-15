package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFEEventStatus {
    PENDENTE("Pendente"),
    EM_PROCESSAMENTO("Em Processamento"),
    AUTORIZADO("Autorizado"),
    REJEITADO("Rejeitado"),
    ERRO("Erro");

    private final String description;
}