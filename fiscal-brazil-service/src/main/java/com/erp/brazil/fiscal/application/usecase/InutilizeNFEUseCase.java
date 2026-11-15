package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.application.dto.NFEDTO;
import com.erp.brazil.fiscal.domain.repository.NFERepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import com.erp.brazil.fiscal.infrastructure.sefaz.SEFAZClient;
import com.erp.brazil.fiscal.infrastructure.certificado.CertificadoDigitalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use case for inutilizing NF-e number ranges.
 */
@Service
@Transactional
public class InutilizeNFEUseCase {

    private final NFERepository nfeRepository;
    private final SEFAZClient sefazClient;
    private final CertificadoDigitalService certificadoService;
    private final EventPublisher eventPublisher;

    public InutilizeNFEUseCase(NFERepository nfeRepository,
                               SEFAZClient sefazClient,
                               CertificadoDigitalService certificadoService,
                               EventPublisher eventPublisher) {
        this.nfeRepository = nfeRepository;
        this.sefazClient = sefazClient;
        this.certificadoService = certificadoService;
        this.eventPublisher = eventPublisher;
    }

    public NFEDTO.InutilizeResponse execute(NFEDTO.InutilizeRequest request) {
        // 1. Validate range doesn't have existing NF-es
        var existing = nfeRepository.findByCompanyIdAndSeriesAndNumberRange(
                request.companyId(), request.series(), request.startNumber(), request.endNumber());
        if (!existing.isEmpty()) {
            return buildErrorResponse(request, "RANGE_IN_USE", "Number range already has issued NF-es");
        }

        // 2. Get certificate
        var cert = certificadoService.getActiveCertificate(request.companyId());
        if (cert.isEmpty()) {
            return buildErrorResponse(request, "NO_CERTIFICATE", "No active digital certificate found");
        }

        // 3. Send inutilization to SEFAZ
        SEFAZClient.SEFAZInutilizeResponse response;
        try {
            response = sefazClient.inutilizeNfe(request.companyId(), request.series(),
                    request.startNumber(), request.endNumber(), request.justification(), cert.get());
        } catch (Exception e) {
            return buildErrorResponse(request, "SEFAZ_ERROR", "SEFAZ communication failed: " + e.getMessage());
        }

        // 4. Process response
        if ("102".equals(response.getCStat())) {
            // Inutilized successfully
            String protocol = response.getProtInut() != null ? response.getProtInut().getInfInut().getNProt() : null;

            var event = new EventDTO.NFEInutilizedEvent(
                    UUID.randomUUID().toString(),
                    null, // No specific NF-e ID for inutilization
                    null, // No access key
                    LocalDateTime.now(),
                    request.companyId(),
                    request.series(),
                    request.startNumber(),
                    request.endNumber(),
                    request.justification(),
                    protocol,
                    response.getXmlContent()
            );
            eventPublisher.publish(event);

            return new NFEDTO.InutilizeResponse(
                    event.eventId(),
                    protocol,
                    "INUTILIZED",
                    LocalDateTime.now()
            );
        } else {
            return buildErrorResponse(request, "INUTILIZATION_FAILED",
                    response.getXMotivo() != null ? response.getXMotivo() : "Unknown error");
        }
    }

    private NFEDTO.InutilizeResponse buildErrorResponse(NFEDTO.InutilizeRequest request, String status, String message) {
        return new NFEDTO.InutilizeResponse(
                UUID.randomUUID().toString(),
                null,
                status,
                LocalDateTime.now()
        );
    }
}