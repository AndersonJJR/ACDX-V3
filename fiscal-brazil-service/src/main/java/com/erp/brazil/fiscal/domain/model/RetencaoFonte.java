package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_retencao_fonte")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetencaoFonte {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 10, nullable = false)
    private RetencaoTipo tipo;

    @Column(name = "company_cnpj", length = 14, nullable = false)
    private String companyCnpj;

    @Column(name = "fornecedor_cnpj", length = 14, nullable = false)
    private String fornecedorCnpj;

    @Column(name = "fornecedor_nome", length = 115)
    private String fornecedorNome;

    @Column(name = "documento_tipo", length = 20)
    private String documentoTipo; // NFE, NFSE, CTE, NOTA_FISCAL_AVULSA

    @Column(name = "documento_numero", length = 50)
    private String documentoNumero;

    @Column(name = "documento_serie", length = 10)
    private String documentoSerie;

    @Column(name = "documento_chave", length = 44)
    private String documentoChave;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "valor_bruto", precision = 15, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "valor_base_calculo", precision = 15, scale = 2)
    private BigDecimal valorBaseCalculo;

    @Column(name = "aliquota", precision = 5, scale = 2)
    private BigDecimal aliquota;

    @Column(name = "valor_retido", precision = 15, scale = 2)
    private BigDecimal valorRetido;

    @Column(name = "codigo_receita", length = 10)
    private String codigoReceita;

    @Column(name = "descricao_servico", columnDefinition = "TEXT")
    private String descricaoServico;

    @Column(name = "cnae_codigo", length = 7)
    private String cnaeCodigo;

    @Column(name = "is_optante_simples")
    private Boolean optanteSimples = false;

    @Column(name = "is_isentou_imune")
    private Boolean isentouImune = false;

    @Column(name = "justificativa_isentou", columnDefinition = "TEXT")
    private String justificativaIsentou;

    @Column(name = "xml_comprovante", columnDefinition = "TEXT")
    private String xmlComprovante;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}