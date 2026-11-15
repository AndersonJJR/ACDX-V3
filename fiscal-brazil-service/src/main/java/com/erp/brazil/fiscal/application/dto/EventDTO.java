package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.NFEEvent;
import com.erp.brazil.fiscal.domain.model.TaxCalculationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for domain events (consumed and published).
 */
public class EventDTO {

    @Schema(description = "Invoice created event - consumed from billing service")
    public record InvoiceCreatedEvent(
            String eventId,
            String invoiceId,
            String companyId,
            String invoiceType,
            String series,
            String number,
            LocalDateTime emissionDate,
            String emitterCnpj,
            String recipientCnpjCpf,
            String ncm,
            String cest,
            String cfop,
            String originState,
            String destinationState,
            BigDecimal totalValue,
            BigDecimal productValue,
            BigDecimal serviceValue,
            List<InvoiceItem> items,
            String taxRegime
    ) {
        public record InvoiceItem(
                int itemNumber,
                String ncm,
                String cest,
                String cfop,
                String ucom,
                BigDecimal quantity,
                BigDecimal unitPrice,
                BigDecimal totalPrice
        ) {}
    }

    @Schema(description = "NF-e emitted event - published to message broker")
    public record NFEEmittedEvent(
            String eventId,
            String nfeId,
            String accessKey,
            String protocol,
            String companyId,
            String series,
            String number,
            LocalDateTime emissionDate,
            String recipientCnpjCpf,
            BigDecimal totalValue,
            String xmlContent,
            String pdfContent,
            LocalDateTime emittedAt
    ) {}

    @Schema(description = "NF-e cancelled event - published to message broker")
    public record NFECancelledEvent(
            String eventId,
            String nfeId,
            String accessKey,
            String protocol,
            String companyId,
            String justification,
            LocalDateTime cancelledAt
    ) {}

    @Schema(description = "SPED generated event - published to message broker")
    public record SPEDGeneratedEvent(
            String eventId,
            String spedId,
            String companyId,
            String type,
            String period,
            String status,
            String filePath,
            String fileHash,
            LocalDateTime generatedAt
    ) {}

    @Schema(description = "Tax calculated event - published to message broker")
    public record TaxCalculatedEvent(
            String eventId,
            String calculationId,
            String companyId,
            String originState,
            String destinationState,
            String ncm,
            String cfop,
            BigDecimal totalTaxes,
            TaxCalculationResult result,
            LocalDateTime calculatedAt
    ) {}

    @Schema(description = "Retention calculated event - published to message broker")
    public record RetencaoCalculatedEvent(
            String eventId,
            String calculationId,
            String companyId,
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            BigDecimal totalRetido,
            BigDecimal netValue,
            List<RetencaoItem> retencoes,
            LocalDateTime calculatedAt
    ) {
        public record RetencaoItem(
                String type,
                BigDecimal baseValue,
                BigDecimal aliquota,
                BigDecimal calculatedValue
        ) {}
    }

    @Schema(description = "Certificate expiry alert event - published to message broker")
    public record CertificadoExpiryAlertEvent(
            String eventId,
            String certificateId,
            String companyId,
            String alias,
            String cnpj,
            String companyName,
            LocalDateTime expiryDate,
            int daysUntilExpiry,
            CertificadoDigitalA1.CertificateStatus status,
            LocalDateTime alertedAt
    ) {}

    @Schema(description = "Leis sync completed event - published to message broker")
    public record LeisSyncCompletedEvent(
            String eventId,
            String version,
            int ncmUpdated,
            int cestUpdated,
            int cfopUpdated,
            int taxRulesUpdated,
            LocalDateTime syncedAt,
            String status
    ) {}
}