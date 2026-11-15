package com.erp.brazil.fiscal.presentation.rest;

import com.erp.brazil.fiscal.application.dto.TaxCalculationDTO;
import com.erp.brazil.fiscal.application.usecase.CalculateTaxesUseCase;
import com.erp.brazil.fiscal.domain.model.TaxCalculationContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for tax calculation operations.
 */
@RestController
@RequestMapping("/api/v1/tax")
@Tag(name = "Tax Calculation", description = "Brazilian tax calculation APIs (ICMS, IPI, PIS, COFINS, ISS)")
public class TaxCalculationController {

    private final CalculateTaxesUseCase calculateTaxesUseCase;

    public TaxCalculationController(CalculateTaxesUseCase calculateTaxesUseCase) {
        this.calculateTaxesUseCase = calculateTaxesUseCase;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate taxes for an operation",
            description = "Calculates all applicable taxes (ICMS, IPI, PIS, COFINS, ISS) based on the provided context")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tax calculation successful",
                    content = @Content(schema = @Schema(implementation = TaxCalculationDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TaxCalculationDTO.Response> calculate(
            @Valid @RequestBody TaxCalculationDTO.Request request) {

        var result = calculateTaxesUseCase.execute(request.context());

        var response = new TaxCalculationDTO.Response(
                result,
                java.util.UUID.randomUUID().toString(),
                java.time.LocalDateTime.now(),
                Map.of()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate/batch")
    @Operation(summary = "Calculate taxes for multiple operations in batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch calculation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TaxCalculationDTO.BatchResponse> calculateBatch(
            @Valid @RequestBody TaxCalculationDTO.BatchRequest request) {

        List<TaxCalculationDTO.Response> results = request.calculations().stream()
                .map(req -> {
                    var result = calculateTaxesUseCase.execute(req.context());
                    return new TaxCalculationDTO.Response(
                            result,
                            java.util.UUID.randomUUID().toString(),
                            java.time.LocalDateTime.now(),
                            Map.of()
                    );
                })
                .toList();

        long successCount = results.stream().filter(r -> r.result().getTotalTaxes() != null).count();

        var response = new TaxCalculationDTO.BatchResponse(
                results,
                (int) successCount,
                request.calculations().size() - (int) successCount,
                java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rules/{taxType}")
    @Operation(summary = "Get active tax rules by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tax rules"),
            @ApiResponse(responseCode = "404", description = "Tax type not found")
    })
    public ResponseEntity<List<TaxCalculationDTO.TaxRuleDTO>> getRulesByType(
            @PathVariable String taxType,
            @RequestParam(required = false) String ncm,
            @RequestParam(required = false) String uf) {

        // This would query the TaxRuleRepository
        return ResponseEntity.ok(List.of());
    }
}