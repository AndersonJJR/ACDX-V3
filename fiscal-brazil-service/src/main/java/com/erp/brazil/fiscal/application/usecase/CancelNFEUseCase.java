package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.application.dto.NFEDTO;
import com.erp.brazil.fiscal.domain.model.NFE;
import com.erp.brazil.fiscal.domain.model.NFEEvent;
import com.erp.brazil.fiscal.domain.repository.NFERepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import com.erp.brazil.fiscal.infrastructure.sefaz.SEFAZClient;
import com.erp.brazil.fiscal.infrastructure.certificado.CertificadoDigitalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case for cancelling NF-e.
 */
@Service
@Transactional
public class CancelNFEUseCase {

    private final NFERepository nfeRepository;
    private final SEFAZClient sefazClient;
    private final CertificadoDigitalService certificadoService;
    private final EventPublisher eventPublisher;

    public CancelNFEUseCase(NFERepository nfeRepository,
                            SEFAZClient sefazClient,
                            CertificadoDigitalService certificadoService,
                            EventPublisher eventPublisher) {
        this.nfeRepository = nfeRepository;
        this.sefazClient = sefazClient;
        this.certificadoService = certificadoService;
        this.eventPublisher = eventPublisher;
    }

    public NFEDTO.CancelResponse execute(NFEDTO.CancelRequest request) {
        // 1. Find NF-e by access key
        var nfeOpt = nfeRepository.findByAccessKey(request.accessKey());
        if (nfeOpt.isEmpty()) {
            return buildErrorResponse(request.accessKey(), "NFE_NOT_FOUND", "NF-e not found");
        }

        NFE nfe = nfeOpt.get();

        // 2. Validate cancellation rules
        if (!canCancel(nfe)) {
            return buildErrorResponse(request.accessKey(), "CANCELLATION_NOT_ALLOWED",
                    "NF-e cannot be cancelled. Status: " + nfe.status() + ", Time since emission: " +
                            java.time.Duration.between(nfe.emissionDate(), LocalDateTime.now()).toHours() + " hours");
        }

        // 3. Get certificate
        var cert = certificadoService.getActiveCertificate(nfe.companyId());
        if (cert.isEmpty()) {
            return buildErrorResponse(request.accessKey(), "NO_CERTIFICATE", "No active digital certificate found");
        }

        // 4. Send cancellation to SEFAZ
        SEFAZClient.SEFAZCancelResponse response;
        try {
            response = sefazClient.cancelNfe(nfe.companyId(), request.accessKey(), request.protocol(),
                    request.justification(), cert.get());
        } catch (Exception e) {
            return buildErrorResponse(request.accessKey(), "SEFAZ_ERROR", "SEFAZ communication failed: " + e.getMessage());
        }

        // 5. Process response
        if ("135".equals(response.getCStat()) || "155".equals(response.getCStat())) {
            // Cancelled successfully
            NFE cancelledNfe = new NFE(
                    nfe.id(), nfe.companyId(), nfe.accessKey(), nfe.protocol(), "CANCELLED",
                    nfe.type(), nfe.series(), nfe.number(), nfe.emissionDate(), nfe.saEntradaDate(),
                    nfe.emitter(), nfe.recipient(), nfe.items(), nfe.totals(),
                    nfe.transport(), nfe.payment(), nfe.additionalInfo(),
                    nfe.xmlContent(), response.getXmlContent(),
                    nfe.createdAt(), LocalDateTime.now(),
                    LocalDateTime.now(), request.justification()
            );
            nfeRepository.save(cancelledNfe);

            // Publish event
            var event = new EventDTO.NFECancelledEvent(
                    UUID.randomUUID().toString(),
                    nfe.id(),
                    nfe.accessKey(),
                    response.getProtCanc() != null ? response.getProtCanc().getInfCanc().getNProt() : null,
                    nfe.companyId(),
                    request.justification(),
                    LocalDateTime.now()
            );
            eventPublisher.publish(event);

            return new NFEDTO.CancelResponse(
                    event.eventId(),
                    nfe.accessKey(),
                    response.getProtCanc() != null ? response.getProtCanc().getInfCanc().getNProt() : null,
                    "CANCELLED",
                    LocalDateTime.now()
            );
        } else {
            return buildErrorResponse(request.accessKey(), "CANCELLATION_FAILED",
                    response.getXMotivo() != null ? response.getXMotivo() : "Unknown error");
        }
    }

    private boolean canCancel(NFE nfe) {
        // Can only cancel if AUTHORIZED and within 24 hours (configurable)
        if (!"AUTHORIZED".equals(nfe.status())) {
            return false;
        }

        long hoursSinceEmission = java.time.Duration.between(nfe.emissionDate(), LocalDateTime.now()).toHours();
        return hoursSinceEmission <= 24; // Brazilian law: 24 hours for cancellation
    }

    private NFEDTO.CancelResponse buildErrorResponse(String accessKey, String status, String message) {
        return new NFEDTO.CancelResponse(
                UUID.randomUUID().toString(),
                accessKey,
                null,
                status,
                LocalDateTime.now()
        );
    }
}