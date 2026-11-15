package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SPEDStatus {
    GERADO("Gerado"),
    VALIDADO("Validado"),
    COM_ERROS("Com Erros"),
    TRANSMITIDO("Transmitido"),
    AUTORIZADO("Autorizado"),
    REJEITADO("Rejeitado");

    private final String description;
}