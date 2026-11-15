package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntegracaoProvedor {
    SEFAZ("SEFAZ", "State SEFAZ"),
    SEFAZ_VIRTUAL("SEFAZ_VIRTUAL", "Virtual SEFAZ (RS/SP)"),
    NFE4J("NFE4J", "NFe4J Library"),
    FOCUS_NFE("FOCUS_NFE", "Focus NFe"),
    TECNOSPEED("TECNOSPEED", "TecnoSpeed"),
    EINVOICE("EINVOICE", "e-Invoice"),
    NFE_IO("NFE_IO", "NFe.io"),
    MUNICIPAL("MUNICIPAL", "Municipal Government"),
    ABRASF("ABRASF", "ABRASF Standard"),
    GINFES("GINFES", "Ginfes"),
    DSFS("DSFS", "DSFS"),
    SIMPLE("SIMPLE", "Simple"),
    CUSTOM("CUSTOM", "Custom Provider");

    private final String code;
    private final String description;
}