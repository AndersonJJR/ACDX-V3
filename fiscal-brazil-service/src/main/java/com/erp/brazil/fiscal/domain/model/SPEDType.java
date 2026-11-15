package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SPEDType {
    ECD("ECD", "Escrituração Contábil Digital"),
    ECF("ECF", "Escrituração Contábil Fiscal"),
    EFD_ICMS_IPI("EFD_ICMS_IPI", "Escrituração Fiscal Digital - ICMS/IPI"),
    EFD_CONTRIBUICOES("EFD_CONTRIBUICOES", "Escrituração Fiscal Digital - Contribuições (PIS/COFINS)");

    private final String code;
    private final String description;
}