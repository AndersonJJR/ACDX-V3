package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.RetencaoFonte;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTOs for retention calculation API.
 */
public class RetencaoDTO {

    @Schema(description = "Retention calculation request for service invoice")
    public record CalculateRequest(
            @NotNull @Valid ServiceInvoice invoice,
            List<RetencaoFonte.RetencaoType> types
    ) {
        public enum RetencaoType {
            IRRF, CSLL, PIS, COFINS, INSS
        }
    }

    @Schema(description = "Service invoice data for retention calculation")
    public record ServiceInvoice(
            String documentId,
            String documentType,
            LocalDate competenceDate,
            String providerCnpjCpf,
            String providerName,
            String providerCityCode,
            String providerState,
            String serviceCode,
            String cnaeCode,
            BigDecimal grossValue,
            BigDecimal deductions,
            String taxRegime,
            boolean isSimplesNacional,
            Map<String, Object> additionalData
    ) {}

    @Schema(description = "Retention calculation response")
    public record CalculateResponse(
            String calculationId,
            BigDecimal baseValue,
            List<RetencaoItem> retencoes,
            BigDecimal totalRetido,
            BigDecimal netValue,
            LocalDate calculatedAt,
            Map<String, Object> warnings
    ) {}

    @Schema(description = "Individual retention item")
    public record RetencaoItem(
            String type,
            BigDecimal baseValue,
            BigDecimal aliquota,
            BigDecimal deducao,
            BigDecimal calculatedValue,
            String natureCode,
            String description
    ) {}

    @Schema(description = "Retention query response")
    public record QueryResponse(
            List<RetencaoFonte> retencoes,
            BigDecimal totalIrrf,
            BigDecimal totalCsll,
            BigDecimal totalPis,
            BigDecimal totalCofins,
            BigDecimal totalInss,
            BigDecimal grandTotal
    ) {}
}