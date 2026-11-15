package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.NFE;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for NF-e API.
 */
public class NFEDTO {

    @Schema(description = "NF-e emission request")
    public record EmitRequest(
            @NotNull @Valid NFE nfe,
            boolean async,
            boolean generatePdf,
            String callbackUrl
    ) {}

    @Schema(description = "NF-e emission response")
    public record EmitResponse(
            String nfeId,
            String accessKey,
            String protocol,
            String status,
            String message,
            LocalDateTime emittedAt,
            String xmlContent,
            String pdfContent
    ) {}

    @Schema(description = "NF-e cancellation request")
    public record CancelRequest(
            @NotBlank String accessKey,
            @NotBlank String justification,
            String protocol
    ) {}

    @Schema(description = "NF-e cancellation response")
    public record CancelResponse(
            String eventId,
            String accessKey,
            String protocol,
            String status,
            LocalDateTime cancelledAt
    ) {}

    @Schema(description = "NF-e inutilization request")
    public record InutilizeRequest(
            @NotBlank String companyId,
            @NotBlank String series,
            int startNumber,
            int endNumber,
            @NotBlank String justification
    ) {}

    @Schema(description = "NF-e inutilization response")
    public record InutilizeResponse(
            String eventId,
            String protocol,
            String status,
            LocalDateTime inutilizedAt
    ) {}

    @Schema(description = "NF-e query response")
    public record QueryResponse(
            NFE nfe,
            String status,
            String protocol,
            LocalDateTime lastCheck,
            List<String> events
    ) {}

    @Schema(description = "NF-e list response")
    public record ListResponse(
            List<NFE> nfes,
            long total,
            int page,
            int size
    ) {}

    @Schema(description = "NF-e status enum")
    public enum NFEStatus {
        PENDING, PROCESSING, EMITTED, AUTHORIZED, DENIED, CANCELLED, INUTILIZED, ERROR
    }
}