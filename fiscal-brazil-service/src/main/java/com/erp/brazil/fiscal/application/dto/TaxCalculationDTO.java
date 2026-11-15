package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.TaxCalculationContext;
import com.erp.brazil.fiscal.domain.model.TaxCalculationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTOs for tax calculation API.
 */
public class TaxCalculationDTO {

    @Schema(description = "Tax calculation request")
    public record Request(
            @NotNull @Valid TaxCalculationContext context,
            List<String> taxTypes,
            boolean includeDetails
    ) {}

    @Schema(description = "Tax calculation response")
    public record Response(
            TaxCalculationResult result,
            String calculationId,
            java.time.LocalDateTime calculatedAt,
            Map<String, Object> warnings
    ) {}

    @Schema(description = "Batch tax calculation request")
    public record BatchRequest(
            List<Request> calculations
    ) {}

    @Schema(description = "Batch tax calculation response")
    public record BatchResponse(
            List<Response> results,
            int successCount,
            int errorCount,
            java.time.LocalDateTime processedAt
    ) {}

    @Schema(description = "Tax rule for configuration")
    public record TaxRuleDTO(
            String id,
            String taxType,
            String name,
            String description,
            String ufOrigin,
            String ufDestination,
            String ncmCode,
            String cestCode,
            String cfopCode,
            String taxRegime,
            java.time.LocalDate effectiveFrom,
            java.time.LocalDate effectiveTo,
            int priority,
            boolean active,
            Map<String, Object> parameters
    ) {}
}