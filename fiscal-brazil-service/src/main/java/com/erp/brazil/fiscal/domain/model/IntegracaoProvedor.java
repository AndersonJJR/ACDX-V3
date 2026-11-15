package com.erp.brazil.fiscal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IntegracaoProvedor {
    SEFAZ("SEFAZ", "SEFAZ Estadual"),
    SEFAZ_VIRTUAL("SEFAZ_VIRTUAL", "SEFAZ Virtual (RS/SP)"),
    NFe4J("NFE4J", "Biblioteca NFe4J"),
    FOCUS_NFE("FOCUS_NFE", "Focus NFe"),
    TECNOSPEED("TECNOSPEED", "TecnoSpeed"),
    EINVOICE("EINVOICE", "e-Invoice"),
    NFE_IO("NFE_IO", "NFe.io"),
    MUNICIPAL("MUNICIPAL", "Prefeitura Municipal"),
    ABRASF("ABRASF", "Padrão ABRASF"),
    GINFES("GINFES", "Ginfes"),
    DSFS("DSFS", "DSFS"),
    SIMPLE("SIMPLE", "Simple"),
    CUSTOM("CUSTOM", "Provedor Customizado");

    private final String code;
    private final String description;
}