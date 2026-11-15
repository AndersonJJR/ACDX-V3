package com.erp.brazil.fiscal.presentation.rest.dto;

import com.erp.brazil.fiscal.domain.model.NFEStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NFEResponseDTO {
    private String id;
    private String chaveAcesso;
    private Long numero;
    private String serie;
    private String modelo;
    private String naturezaOperacao;
    private NFEStatus status;
    private String emitenteCnpj;
    private String emitenteNome;
    private String emitenteUf;
    private String destinatarioCnpj;
    private String destinatarioNome;
    private String destinatarioUf;
    private LocalDateTime dataEmissao;
    private LocalDateTime dataSaidaEntrada;
    private BigDecimal valorTotal;
    private BigDecimal valorIcms;
    private BigDecimal valorIcmsSt;
    private BigDecimal valorIpi;
    private BigDecimal valorPis;
    private BigDecimal valorCofins;
    private String protocoloAutorizacao;
    private String xmlAutorizado;
    private String ambiente;
    private String versao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime authorizedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String cancellationProtocol;
}