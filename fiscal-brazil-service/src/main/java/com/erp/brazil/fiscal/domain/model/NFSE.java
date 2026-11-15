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
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_nfse")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NFSE {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "numero", nullable = false)
    private Long numero;

    @Column(name = "serie", length = 5)
    private String serie;

    @Column(name = "codigo_verificacao", length = 50)
    private String codigoVerificacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private NFSEStatus status = NFSEStatus.PENDENTE;

    @Column(name = "prestador_cnpj", length = 14, nullable = false)
    private String prestadorCnpj;

    @Column(name = "prestador_nome", length = 115, nullable = false)
    private String prestadorNome;

    @Column(name = "prestador_inscricao_municipal", length = 15)
    private String prestadorInscricaoMunicipal;

    @Column(name = "prestador_municipio", length = 7)
    private String prestadorMunicipio;

    @Column(name = "tomador_cnpj", length = 14)
    private String tomadorCnpj;

    @Column(name = "tomador_nome", length = 115)
    private String tomadorNome;

    @Column(name = "tomador_inscricao_municipal", length = 15)
    private String tomadorInscricaoMunicipal;

    @Column(name = "tomador_municipio", length = 7)
    private String tomadorMunicipio;

    @Column(name = "servico_codigo", length = 20)
    private String servicoCodigo;

    @Column(name = "servico_descricao", length = 2000)
    private String servicoDescricao;

    @Column(name = "servico_cnae", length = 7)
    private String servicoCnae;

    @Column(name = "aliquota_iss", precision = 5, scale = 2)
    private BigDecimal aliquotaIss;

    @Column(name = "valor_servicos", precision = 15, scale = 2)
    private BigDecimal valorServicos;

    @Column(name = "valor_deducoes", precision = 15, scale = 2)
    private BigDecimal valorDeducoes;

    @Column(name = "valor_iss", precision = 15, scale = 2)
    private BigDecimal valorIss;

    @Column(name = "valor_iss_retido", precision = 15, scale = 2)
    private BigDecimal valorIssRetido;

    @Column(name = "valor_irrf", precision = 15, scale = 2)
    private BigDecimal valorIrrf;

    @Column(name = "valor_pis", precision = 15, scale = 2)
    private BigDecimal valorPis;

    @Column(name = "valor_cofins", precision = 15, scale = 2)
    private BigDecimal valorCofins;

    @Column(name = "valor_csll", precision = 15, scale = 2)
    private BigDecimal valorCsll;

    @Column(name = "valor_inss", precision = 15, scale = 2)
    private BigDecimal valorInss;

    @Column(name = "base_calculo_iss", precision = 15, scale = 2)
    private BigDecimal baseCalculoIss;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "data_competencia")
    private LocalDateTime dataCompetencia;

    @Column(name = "rps_numero")
    private Long rpsNumero;

    @Column(name = "rps_serie", length = 5)
    private String rpsSerie;

    @Column(name = "rps_tipo", length = 1)
    private String rpsTipo = "1"; // 1=RPS, 2=NFS-e

    @Column(name = "xml_envio", columnDefinition = "TEXT")
    private String xmlEnvio;

    @Column(name = "xml_retorno", columnDefinition = "TEXT")
    private String xmlRetorno;

    @Column(name = "protocolo", length = 50)
    private String protocolo;

    @Column(name = "municipio_provedor", length = 100)
    private String municipioProvedor;

    @Column(name = "versao", length = 10)
    private String versao;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;
}