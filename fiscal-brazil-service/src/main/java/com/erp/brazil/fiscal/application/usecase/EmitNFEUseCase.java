package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.application.dto.NFEDTO;
import com.erp.brazil.fiscal.domain.model.NFE;
import com.erp.brazil.fiscal.domain.model.NFEEvent;
import com.erp.brazil.fiscal.domain.repository.NFERepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import com.erp.brazil.fiscal.infrastructure.sefaz.SEFAZClient;
import com.erp.brazil.fiscal.infrastructure.certificado.CertificadoDigitalService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Use case for emitting NF-e asynchronously.
 * Calls SEFAZ, publishes events, handles retries.
 */
@Service
@Transactional
public class EmitNFEUseCase {

    private final NFERepository nfeRepository;
    private final SEFAZClient sefazClient;
    private final CertificadoDigitalService certificadoService;
    private final ValidateNFEUseCase validateUseCase;
    private final EventPublisher eventPublisher;

    public EmitNFEUseCase(NFERepository nfeRepository,
                          SEFAZClient sefazClient,
                          CertificadoDigitalService certificadoService,
                          ValidateNFEUseCase validateUseCase,
                          EventPublisher eventPublisher) {
        this.nfeRepository = nfeRepository;
        this.sefazClient = sefazClient;
        this.certificadoService = certificadoService;
        this.validateUseCase = validateUseCase;
        this.eventPublisher = eventPublisher;
    }

    @Async("nfeExecutor")
    public CompletableFuture<NFEDTO.EmitResponse> executeAsync(NFEDTO.EmitRequest request) {
        return CompletableFuture.supplyAsync(() -> executeSync(request));
    }

    public NFEDTO.EmitResponse executeSync(NFEDTO.EmitRequest request) {
        NFE nfe = request.nfe();

        // 1. Validate NF-e
        var validation = validateUseCase.validate(nfe);
        if (!validation.isValid()) {
            return buildErrorResponse(nfe, "VALIDATION_FAILED", validation.getErrors().toString());
        }

        // 2. Get certificate for signing
        var cert = certificadoService.getActiveCertificate(nfe.companyId());
        if (cert.isEmpty()) {
            return buildErrorResponse(nfe, "NO_CERTIFICATE", "No active digital certificate found for company");
        }

        // 3. Sign NF-e XML
        String signedXml;
        try {
            signedXml = certificadoService.signXml(nfe, cert.get());
        } catch (Exception e) {
            return buildErrorResponse(nfe, "SIGNING_FAILED", "Failed to sign NF-e: " + e.getMessage());
        }

        // 4. Save NF-e with PENDING status
        NFE pendingNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), null, "PENDING",
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), nfe.items(), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                signedXml, null,
                LocalDateTime.now(), LocalDateTime.now(),
                null, null
        );
        nfeRepository.save(pendingNfe);

        // 5. Send to SEFAZ
        SEFAZClient.SEFAZResponse sefazResponse;
        try {
            sefazResponse = sefazClient.sendNfe(nfe.companyId(), signedXml, cert.get());
        } catch (Exception e) {
            updateNfeStatus(pendingNfe.id(), "ERROR", null, e.getMessage());
            return buildErrorResponse(pendingNfe, "SEFAZ_ERROR", "SEFAZ communication failed: " + e.getMessage());
        }

        // 6. Process SEFAZ response
        return processSefazResponse(pendingNfe, sefazResponse, signedXml, request.generatePdf());
    }

    private NFEDTO.EmitResponse processSefazResponse(NFE nfe, SEFAZClient.SEFAZResponse response,
                                                      String signedXml, boolean generatePdf) {
        String status = mapSefazStatus(response.getCStat());
        String protocol = response.getProtNFe() != null ? response.getProtNFe().getInfProt().getNProt() : null;

        NFE updatedNfe = new NFE(
                nfe.id(), nfe.companyId(), nfe.accessKey(), protocol, status,
                nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                nfe.emitter(), nfe.recipient(), nfe.items(), nfe.totals(),
                nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                signedXml, response.getXmlContent(),
                nfe.createdAt(), LocalDateTime.now(),
                null, null
        );
        nfeRepository.save(updatedNfe);

        // Generate PDF if requested
        String pdfContent = null;
        if (generatePdf && "AUTHORIZED".equals(status)) {
            pdfContent = generatePdf(response.getXmlContent());
            updatedNfe = new NFE(
                    updatedNfe.id(), updatedNfe.companyId(), updatedNfe.accessKey(), updatedNfe.protocol(),
                    updatedNfe.status(), updatedNfe.type(), updatedNfe.series(), updatedNfe.number(),
                    updatedNfe.emissionDate(), updatedNfe.saEntradaDate(),
                    updatedNfe.emitter(), updatedNfe.recipient(), updatedNfe.items(), updatedNfe.totals(),
                    updatedNfe.transport(), updatedNfe.payment(), updatedNfe.additionalInfo(),
                    updatedNfe.xmlContent(), updatedNfe.pdfContent(), updatedNfe.createdAt(),
                    updatedNfe.updatedAt(), updatedNfe.cancelledAt(), updatedNfe.cancellationReason()
            );
            nfeRepository.save(updatedNfe);
        }

        // Publish event
        if ("AUTHORIZED".equals(status)) {
            var event = new EventDTO.NFEEmittedEvent(
                    UUID.randomUUID().toString(),
                    updatedNfe.id(),
                    updatedNfe.accessKey(),
                    protocol,
                    updatedNfe.companyId(),
                    updatedNfe.series(),
                    updatedNfe.number(),
                    updatedNfe.emissionDate(),
                    updatedNfe.recipient().cnpjCpf(),
                    updatedNfe.totals().vNF(),
                    updatedNfe.xmlContent(),
                    pdfContent,
                    LocalDateTime.now()
            );
            eventPublisher.publish(event);
        }

        return new NFEDTO.EmitResponse(
                updatedNfe.id(),
                updatedNfe.accessKey(),
                protocol,
                status,
                response.getXMotivo(),
                LocalDateTime.now(),
                updatedNfe.xmlContent(),
                pdfContent
        );
    }

    private String mapSefazStatus(String cStat) {
        return switch (cStat) {
            case "100" -> "AUTHORIZED";      // Autorizado o uso da NF-e
            case "110" -> "AUTHORIZED";      // Autorizado o uso da NF-e em contingência
            case "150" -> "AUTHORIZED";      // Autorizado o uso da NF-e, com mensagem de alerta
            case "201" -> "DENIED";          // Rejeição: NF-e não autorizada
            case "202" -> "DENIED";          // Rejeição: NF-e não autorizada (em contingência)
            case "301" -> "DENIED";          // Uso Denegado: Irregularidade fiscal do emitente
            case "302" -> "DENIED";          // Uso Denegado: Irregularidade fiscal do destinatário
            case "303" -> "DENIED";          // Uso Denegado: Irregularidade fiscal do emitente e destinatário
            default -> "ERROR";
        };
    }

    private String generatePdf(String xmlContent) {
        // Placeholder for PDF generation (using iText or similar)
        return "PDF_CONTENT_PLACEHOLDER";
    }

    private void updateNfeStatus(String id, String status, String protocol, String errorMessage) {
        nfeRepository.findById(id).ifPresent(nfe -> {
            NFE updated = new NFE(
                    nfe.id(), nfe.companyId(), nfe.accessKey(), protocol, status,
                    nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                    nfe.emitter(), nfe.recipient(), nfe.items(), nfe.totals(),
                    nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                    nfe.xmlContent(), nfe.pdfContent(),
                    nfe.createdAt(), LocalDateTime.now(),
                    null, errorMessage
            );
            nfeRepository.save(updated);
        });
    }

    private NFEDTO.EmitResponse buildErrorResponse(NFE nfe, String status, String message) {
        return new NFEDTO.EmitResponse(
                nfe.id(),
                nfe.accessKey(),
                null,
                status,
                message,
                LocalDateTime.now(),
                nfe.xmlContent(),
                null
        );
    }
}