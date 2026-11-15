package com.erp.brazil.fiscal.presentation.rest;

import com.erp.brazil.fiscal.application.dto.RetencaoDTO;
import com.erp.brazil.fiscal.application.usecase.CalculateRetencoesUseCase;
import com.erp.brazil.fiscal.domain.model.RetencaoFonte;
import com.erp.brazil.fiscal.domain.repository.RetencaoFonteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for retention calculation operations.
 */
@RestController
@RequestMapping("/api/v1/retencao")
@Tag(name = "Retenção na Fonte", description = "Source withholding calculation for service invoices (IRRF, CSLL, PIS, COFINS, INSS)")
public class RetencaoController {

    private final CalculateRetencoesUseCase calculateUseCase;
    private final RetencaoFonteRepository retencaoRepository;

    public RetencaoController(CalculateRetencoesUseCase calculateUseCase,
                               RetencaoFonteRepository retencaoRepository) {
        this.calculateUseCase = calculateUseCase;
        this.retencaoRepository = retencaoRepository;
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate retentions for service invoice",
            description = "Calculates IRRF, CSLL, PIS, COFINS, and INSS retentions based on service type and provider regime.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculation completed",
                    content = @Content(schema = @Schema(implementation = RetencaoDTO.CalculateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RetencaoDTO.CalculateResponse> calculate(
            @Valid @RequestBody RetencaoDTO.CalculateRequest request) {

        RetencaoDTO.CalculateResponse response = calculateUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Query retentions by company and period",
            description = "Returns all retentions for a company within a date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retentions found"),
            @ApiResponse(responseCode = "404", description = "No retentions found")
    })
    public ResponseEntity<RetencaoDTO.QueryResponse> query(
            @PathVariable String companyId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<RetencaoFonte> retencoes = retencaoRepository.findByCompanyIdAndCompetenceDateBetween(
                companyId, startDate, endDate);

        BigDecimal totalIrrf = retencoes.stream()
                .filter(r -> r instanceof RetencaoFonte.IRRF)
                .map(RetencaoFonte::getCalculatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCsll = retencoes.stream()
                .filter(r -> r instanceof RetencaoFonte.CSLL)
                .map(RetencaoFonte::getCalculatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPis = retencoes.stream()
                .filter(r -> r instanceof RetencaoFonte.PIS)
                .map(RetencaoFonte::getCalculatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCofins = retencoes.stream()
                .filter(r -> r instanceof RetencaoFonte.COFINS)
                .map(RetencaoFonte::getCalculatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInss = retencoes.stream()
                .filter(r -> r instanceof RetencaoFonte.INSS)
                .map(RetencaoFonte::getCalculatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotal = totalIrrf.add(totalCsll).add(totalPis).add(totalCofins).add(totalInss);

        var response = new RetencaoDTO.QueryResponse(
                retencoes,
                totalIrrf,
                totalCsll,
                totalPis,
                totalCofins,
                totalInss,
                grandTotal
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/document/{documentId}")
    @Operation(summary = "Get retentions by document ID",
            description = "Returns all retentions calculated for a specific document.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retentions found"),
            @ApiResponse(responseCode = "404", description = "No retentions found")
    })
    public ResponseEntity<List<RetencaoFonte>> getByDocument(
            @PathVariable String documentId,
            @RequestParam String companyId) {

        List<RetencaoFonte> retencoes = retencaoRepository.findByCompanyIdAndDocumentId(companyId, documentId);
        return ResponseEntity.ok(retencoes);
    }

    @GetMapping("/provider/{cnpjCpf}")
    @Operation(summary = "Get retentions by provider",
            description = "Returns all retentions for a specific provider within a date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retentions found"),
            @ApiResponse(responseCode = "404", description = "No retentions found")
    })
    public ResponseEntity<List<RetencaoFonte>> getByProvider(
            @PathVariable String cnpjCpf,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<RetencaoFonte> retencoes = retencaoRepository.findByProviderAndCompetenceDateBetween(
                cnpjCpf, startDate, endDate);
        return ResponseEntity.ok(retencoes);
    }
}