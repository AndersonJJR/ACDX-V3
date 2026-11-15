package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RetencaoTipo {
    IRRF("IRRF", "Imposto de Renda Retido na Fonte"),
    CSLL("CSLL", "Contribuição Social sobre o Lucro Líquido"),
    PIS("PIS", "Programa de Integração Social"),
    COFINS("COFINS", "Contribuição para o Financiamento da Seguridade Social"),
    INSS("INSS", "Instituto Nacional do Seguro Social"),
    ISS("ISS", "Imposto Sobre Serviços");

    private final String code;
    private final String description;
}