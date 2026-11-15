package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NFEEventType {
    CANCELAMENTO("110111", "Cancelamento"),
    CARTA_CORRECAO("110110", "Carta de Correção"),
    MANIFESTACAO_DESTINATARIO("210200", "Manifestação do Destinatário"),
    EPEC("110140", "EPEC - Evento Prévio da Emissão em Contingência"),
    CCE("110110", "Carta de Correção Eletrônica");

    private final String code;
    private final String description;
}