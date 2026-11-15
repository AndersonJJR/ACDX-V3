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
@Table(name = "fiscal_cte")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CTE {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "chave_acesso", length = 44, unique = true, nullable = false)
    private String chaveAcesso;

    @Column(name = "numero", nullable = false)
    private Long numero;

    @Column(name = "serie", length = 3)
    private String serie = "1";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CTEStatus status = CTEStatus.PENDENTE;

    @Column(name = "emitente_cnpj", length = 14, nullable = false)
    private String emitenteCnpj;

    @Column(name = "emitente_nome", length = 60, nullable = false)
    private String emitenteNome;

    @Column(name = "emitente_uf", length = 2, nullable = false)
    private String emitenteUf;

    @Column(name = "remetente_cnpj", length = 14)
    private String remetenteCnpj;

    @Column(name = "remetente_nome", length = 60)
    private String remetenteNome;

    @Column(name = "destinatario_cnpj", length = 14)
    private String destinatarioCnpj;

    @Column(name = "destinatario_nome", length = 60)
    private String destinatarioNome;

    @Column(name = "tomador_cnpj", length = 14)
    private String tomadorCnpj;

    @Column(name = "tomador_nome", length = 60)
    private String tomadorNome;

    @Column(name = "cfop", length = 4)
    private String cfop;

    @Column(name = "modalidade", length = 1)
    private String modalidade = "1"; // 1=Rodoviário, 2=Aéreo, 3=Aquaviário, 4=Ferroviário, 5=Dutoviário, 6=Multimodal

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "valor_total", precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_icms", precision = 15, scale = 2)
    private BigDecimal valorIcms;

    @Column(name = "valor_icms_st", precision = 15, scale = 2)
    private BigDecimal valorIcmsSt;

    @Column(name = "protocolo_autorizacao", length = 50)
    private String protocoloAutorizacao;

    @Column(name = "xml_assinado", columnDefinition = "TEXT")
    private String xmlAssinado;

    @Column(name = "xml_autorizado", columnDefinition = "TEXT")
    private String xmlAutorizado;

    @Column(name = "ambiente", length = 1)
    private String ambiente = "1";

    @Column(name = "versao", length = 10)
    private String versao = "4.00";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;
}