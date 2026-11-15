package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RetencaoType {
    IRRF("IRRF", "Withholding Income Tax"),
    CSLL("CSLL", "Social Contribution on Net Profit"),
    PIS("PIS", "Social Integration Program"),
    COFINS("COFINS", "Social Security Financing Contribution"),
    INSS("INSS", "National Institute of Social Security"),
    ISS("ISS", "Service Tax");

    private final String code;
    private final String description;
}