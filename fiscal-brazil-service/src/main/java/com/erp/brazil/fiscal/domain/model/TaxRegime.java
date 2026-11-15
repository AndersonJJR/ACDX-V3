package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaxRegime {
    SIMPLES_NACIONAL("Simples Nacional"),
    LUCRO_PRESUMIDO("Lucro Presumido"),
    LUCRO_REAL("Lucro Real"),
    MEI("MEI");

    private final String description;
}