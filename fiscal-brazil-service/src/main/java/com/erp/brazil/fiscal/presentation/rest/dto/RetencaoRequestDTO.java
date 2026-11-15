package com.erp.brazil.fiscal.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetencaoRequestDTO {
    @NotBlank
    private String companyCnpj;

    @NotBlank
    private String fornecedorCnpj;

    private String fornecedorNome;

    @NotBlank
    private String documentoTipo; // NFE, NFSE, CTE, NOTA_FISCAL_AVULSA

    @NotBlank
    private String documentoNumero;

    private String documentoSerie;

    private String documentoChave;

    @NotNull
    private LocalDate dataEmissao;

    @NotNull
    private LocalDate dataPagamento;

    @NotNull
    private BigDecimal valorBruto;

    private String cnaeCodigo;

    private String descricaoServico;

    private boolean optanteSimples = false;
}