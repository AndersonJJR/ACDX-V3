package com.erp.brazil.fiscal.infrastructure.event;

import com.erp.brazil.fiscal.application.port.EventPublisherPort;
import com.erp.brazil.fiscal.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQEventPublisher implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fiscal.events.exchange:fiscal.events}")
    private String exchange;

    @Override
    public void publishNFEEmitted(NFE nfe) {
        NFEEmittedEvent event = NFEEmittedEvent.from(nfe);
        rabbitTemplate.convertAndSend(exchange, "nfe.emitted", event);
        log.debug("Published NFEEmittedEvent for NF-e: {}", nfe.getChaveAcesso());
    }

    @Override
    public void publishNFECancelled(NFE nfe) {
        NFECancelledEvent event = NFECancelledEvent.from(nfe);
        rabbitTemplate.convertAndSend(exchange, "nfe.cancelled", event);
        log.debug("Published NFECancelledEvent for NF-e: {}", nfe.getChaveAcesso());
    }

    @Override
    public void publishNFSEEmitted(NFSE nfse) {
        NFSEEmittedEvent event = NFSEEmittedEvent.from(nfse);
        rabbitTemplate.convertAndSend(exchange, "nfse.emitted", event);
        log.debug("Published NFSEEmittedEvent for NFS-e: {}", nfse.getNumero());
    }

    @Override
    public void publishNFSECancelled(NFSE nfse) {
        NFSECancelledEvent event = NFSECancelledEvent.from(nfse);
        rabbitTemplate.convertAndSend(exchange, "nfse.cancelled", event);
        log.debug("Published NFSECancelledEvent for NFS-e: {}", nfse.getNumero());
    }

    @Override
    public void publishCTEEmitted(CTE cte) {
        CTEEmittedEvent event = CTEEmittedEvent.from(cte);
        rabbitTemplate.convertAndSend(exchange, "cte.emitted", event);
        log.debug("Published CTEEmittedEvent for CT-e: {}", cte.getChaveAcesso());
    }

    @Override
    public void publishCTECancelled(CTE cte) {
        CTecancelledEvent event = CTecancelledEvent.from(cte);
        rabbitTemplate.convertAndSend(exchange, "cte.cancelled", event);
        log.debug("Published CTecancelledEvent for CT-e: {}", cte.getChaveAcesso());
    }

    @Override
    public void publishSPEDGenerated(SPEDBlock spedBlock) {
        SPEDGeneratedEvent event = SPEDGeneratedEvent.from(spedBlock);
        rabbitTemplate.convertAndSend(exchange, "sped.generated", event);
        log.debug("Published SPEDGeneratedEvent for block: {}-{}", spedBlock.getBlock(), spedBlock.getRegister());
    }

    @Override
    public void publishTaxCalculated(String companyCnpj, TaxCalculationResult result) {
        TaxCalculatedEvent event = TaxCalculatedEvent.from(companyCnpj, result);
        rabbitTemplate.convertAndSend(exchange, "tax.calculated", event);
        log.debug("Published TaxCalculatedEvent for company: {}", companyCnpj);
    }

    @Override
    public void publishRetencaoCalculated(RetencaoFonte retencao) {
        RetencaoCalculatedEvent event = RetencaoCalculatedEvent.from(retencao);
        rabbitTemplate.convertAndSend(exchange, "retencao.calculated", event);
        log.debug("Published RetencaoCalculatedEvent for supplier: {}", retencao.getFornecedorCnpj());
    }

    // Event DTOs
    public record NFEEmittedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String companyCnpj,
            String chaveAcesso,
            Long numero,
            String serie,
            String modelo,
            String emitenteCnpj,
            String emitenteNome,
            String destinatarioCnpj,
            String destinatarioNome,
            BigDecimal valorTotal,
            String protocoloAutorizacao,
            LocalDateTime authorizedAt
    ) {
        public static NFEEmittedEvent from(NFE nfe) {
            return new NFEEmittedEvent(
                    java.util.UUID.randomUUID().toString(),
                    "NFE_EMITTED",
                    LocalDateTime.now(),
                    nfe.getEmitenteCnpj(),
                    nfe.getChaveAcesso(),
                    nfe.getNumero(),
                    nfe.getSerie(),
                    nfe.getModelo(),
                    nfe.getEmitenteCnpj(),
                    nfe.getEmitenteNome(),
                    nfe.getDestinatarioCnpj(),
                    nfe.getDestinatarioNome(),
                    nfe.getValorTotal(),
                    nfe.getProtocoloAutorizacao(),
                    nfe.getAuthorizedAt()
            );
        }
    }

    public record NFECancelledEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String companyCnpj,
            String chaveAcesso,
            String protocoloCancelamento,
            String justificativa,
            LocalDateTime cancelledAt
    ) {
        public static NFECancelledEvent from(NFE nfe) {
            return new NFECancelledEvent(
                    java.util.UUID.randomUUID().toString(),
                    "NFE_CANCELLED",
                    LocalDateTime.now(),
                    nfe.getEmitenteCnpj(),
                    nfe.getChaveAcesso(),
                    nfe.getCancellationProtocol(),
                    nfe.getCancellationReason(),
                    nfe.getCancelledAt()
            );
        }
    }

    public record NFSEEmittedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String prestadorCnpj,
            Long numero,
            String serie,
            String codigoVerificacao,
            BigDecimal valorServicos,
            BigDecimal valorIss,
            String protocolo,
            LocalDateTime authorizedAt
    ) {
        public static NFSEEmittedEvent from(NFSE nfse) {
            return new NFSEEmittedEvent(
                    java.util.UUID.randomUUID().toString(),
                    "NFSE_EMITTED",
                    LocalDateTime.now(),
                    nfse.getPrestadorCnpj(),
                    nfse.getNumero(),
                    nfse.getSerie(),
                    nfse.getCodigoVerificacao(),
                    nfse.getValorServicos(),
                    nfse.getValorIss(),
                    nfse.getProtocolo(),
                    nfse.getDataEmissao()
            );
        }
    }

    public record NFSECancelledEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String prestadorCnpj,
            Long numero,
            String serie,
            String justificativa,
            LocalDateTime cancelledAt
    ) {
        public static NFSECancelledEvent from(NFSE nfse) {
            return new NFSECancelledEvent(
                    java.util.UUID.randomUUID().toString(),
                    "NFSE_CANCELLED",
                    LocalDateTime.now(),
                    nfse.getPrestadorCnpj(),
                    nfse.getNumero(),
                    nfse.getSerie(),
                    nfse.getCancellationReason(),
                    nfse.getCancelledAt()
            );
        }
    }

    public record CTEEmittedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String emitenteCnpj,
            String chaveAcesso,
            Long numero,
            String serie,
            String remetenteCnpj,
            String destinatarioCnpj,
            BigDecimal valorTotal,
            String protocoloAutorizacao,
            LocalDateTime authorizedAt
    ) {
        public static CTEEmittedEvent from(CTE cte) {
            return new CTEEmittedEvent(
                    java.util.UUID.randomUUID().toString(),
                    "CTE_EMITTED",
                    LocalDateTime.now(),
                    cte.getEmitenteCnpj(),
                    cte.getChaveAcesso(),
                    cte.getNumero(),
                    cte.getSerie(),
                    cte.getRemetenteCnpj(),
                    cte.getDestinatarioCnpj(),
                    cte.getValorTotal(),
                    cte.getProtocoloAutorizacao(),
                    cte.getAuthorizedAt()
            );
        }
    }

    public record CTecancelledEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String emitenteCnpj,
            String chaveAcesso,
            String protocoloCancelamento,
            String justificativa,
            LocalDateTime cancelledAt
    ) {
        public static CTecancelledEvent from(CTE cte) {
            return new CTecancelledEvent(
                    java.util.UUID.randomUUID().toString(),
                    "CTE_CANCELLED",
                    LocalDateTime.now(),
                    cte.getEmitenteCnpj(),
                    cte.getChaveAcesso(),
                    cte.getCancellationProtocol(),
                    cte.getCancellationReason(),
                    cte.getCancelledAt()
            );
        }
    }

    public record SPEDGeneratedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String companyCnpj,
            String spedType,
            String referenceMonth,
            String block,
            String register,
            int lineNumber
    ) {
        public static SPEDGeneratedEvent from(SPEDBlock block) {
            return new SPEDGeneratedEvent(
                    java.util.UUID.randomUUID().toString(),
                    "SPED_GENERATED",
                    LocalDateTime.now(),
                    block.getCompanyCnpj(),
                    block.getSpedType().name(),
                    block.getReferenceMonth(),
                    block.getBlock(),
                    block.getRegister(),
                    block.getLineNumber().intValue()
            );
        }
    }

    public record TaxCalculatedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String companyCnpj,
            String operationType,
            BigDecimal totalTaxes,
            BigDecimal totalValueWithTaxes,
            BigDecimal icms,
            BigDecimal ipi,
            BigDecimal pis,
            BigDecimal cofins,
            BigDecimal iss,
            BigDecimal irrf,
            BigDecimal csll,
            BigDecimal inss
    ) {
        public static TaxCalculatedEvent from(String companyCnpj, TaxCalculationResult result) {
            return new TaxCalculatedEvent(
                    java.util.UUID.randomUUID().toString(),
                    "TAX_CALCULATED",
                    LocalDateTime.now(),
                    companyCnpj,
                    null,
                    result.getTotalTaxes(),
                    result.getTotalValueWithTaxes(),
                    result.getValueIcms(),
                    result.getValueIpi(),
                    result.getValuePis(),
                    result.getValueCofins(),
                    result.getValueIss(),
                    result.getValueIrrf(),
                    result.getValueCsll(),
                    result.getValueInss()
            );
        }
    }

    public record RetencaoCalculatedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String companyCnpj,
            String fornecedorCnpj,
            String tipo,
            BigDecimal valorBruto,
            BigDecimal valorRetido,
            String documentoNumero,
            LocalDate dataEmissao
    ) {
        public static RetencaoCalculatedEvent from(RetencaoFonte retencao) {
            return new RetencaoCalculatedEvent(
                    java.util.UUID.randomUUID().toString(),
                    "RETENCAO_CALCULATED",
                    LocalDateTime.now(),
                    retencao.getCompanyCnpj(),
                    retencao.getFornecedorCnpj(),
                    retencao.getTipo().name(),
                    retencao.getValorBruto(),
                    retencao.getValorRetido(),
                    retencao.getDocumentoNumero(),
                    retencao.getDataEmissao()
            );
        }
    }
}