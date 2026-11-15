package com.erp.brazil.fiscal.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NFERequestDTO {
    @NotBlank
    private String emitenteCnpj;

    @NotBlank
    private String emitenteNome;

    @NotBlank
    private String emitenteUf;

    private String destinatarioCnpj;

    private String destinatarioNome;

    private String destinatarioUf;

    @NotNull
    private LocalDateTime dataEmissao;

    private LocalDateTime dataSaidaEntrada;

    @NotBlank
    private String naturezaOperacao;

    @NotNull
    private BigDecimal valorTotal;

    private BigDecimal valorIcms;

    private BigDecimal valorIcmsSt;

    private BigDecimal valorIpi;

    private BigDecimal valorPis;

    private BigDecimal valorCofins;

    private String ambiente = "1";

    private String modelo = "55";

    private String serie = "1";

    private String finalidadeEmissao = "1";

    private String processoEmissao = "0";

    private String xmlContent;

    private List<NFEItemDTO> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NFEItemDTO {
    private int numeroItem;
    private String codigoProduto;
    private String ncm;
    private String cest;
    private String cfop;
    private String unidade;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private BigDecimal valorDesconto;
    private BigDecimal valorFrete;
    private BigDecimal valorSeguro;
    private BigDecimal valorOutrasDespesas;
    private String icmsCst;
    private String icmsCsosn;
    private BigDecimal icmsAliquota;
    private BigDecimal icmsValor;
    private String ipiCst;
    private BigDecimal ipiAliquota;
    private BigDecimal ipiValor;
    private String pisCst;
    private BigDecimal pisAliquota;
    private BigDecimal pisValor;
    private String cofinsCst;
    private BigDecimal cofinsAliquota;
    private BigDecimal cofinsValor;
}