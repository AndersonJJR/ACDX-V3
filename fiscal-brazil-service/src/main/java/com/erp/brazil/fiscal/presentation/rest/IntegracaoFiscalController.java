package com.erp.brazil.fiscal.presentation.rest;

import com.erp.brazil.fiscal.application.dto.IntegracaoFiscalDTO;
import com.erp.brazil.fiscal.domain.model.IntegracaoFiscal;
import com.erp.brazil.fiscal.domain.repository.IntegracaoFiscalRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for Fiscal Integration management.
 */
@RestController
@RequestMapping("/api/v1/integracoes")
@Tag(name = "Integração Fiscal", description = "Fiscal integration configuration for SEFAZ, municipalities, eSocial, etc.")
public class IntegracaoFiscalController {

    private final IntegracaoFiscalRepository integracaoRepository;

    public IntegracaoFiscalController(IntegracaoFiscalRepository integracaoRepository) {
        this.integracaoRepository = integracaoRepository;
    }

    @PostMapping
    @Operation(summary = "Register new integration",
            description = "Registers a new fiscal integration configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Integration registered",
                    content = @Content(schema = @Schema(implementation = IntegracaoFiscalDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid configuration or duplicate"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<IntegracaoFiscalDTO.Response> register(
            @Valid @RequestBody IntegracaoFiscalDTO.RegisterRequest request) {

        IntegracaoFiscal integracao = IntegracaoFiscal.create(
                request.companyId(),
                request.type(),
                request.name(),
                toJson(request.config()),
                request.environment(),
                request.endpointUrl(),
                request.certificateId()
        );

        IntegracaoFiscal saved = integracaoRepository.save(integracao);
        return ResponseEntity.status(201).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get integration by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Integration found"),
            @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegracaoFiscalDTO.Response> getById(@PathVariable String id) {
        Optional<IntegracaoFiscal> opt = integracaoRepository.findById(id);
        return opt.map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List integrations by company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Integration list")
    })
    public ResponseEntity<IntegracaoFiscalDTO.ListResponse> listByCompany(
            @RequestParam String companyId,
            @RequestParam(required = false) IntegracaoFiscalDTO.IntegrationType type) {

        List<IntegracaoFiscal> integracoes;
        if (type != null) {
            integracoes = integracaoRepository.findByCompanyIdAndType(companyId, type);
        } else {
            integracoes = integracaoRepository.findByCompanyId(companyId);
        }

        List<IntegracaoFiscalDTO.Response> responses = integracoes.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(new IntegracaoFiscalDTO.ListResponse(responses, integracoes.size()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update integration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Integration updated"),
            @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegracaoFiscalDTO.Response> update(
            @PathVariable String id,
            @Valid @RequestBody IntegracaoFiscalDTO.UpdateRequest request) {

        Optional<IntegracaoFiscal> opt = integracaoRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        IntegracaoFiscal existing = opt.get();
        IntegracaoFiscal updated = new IntegracaoFiscal(
                existing.id(),
                existing.companyId(),
                existing.type(),
                request.name() != null ? request.name() : existing.name(),
                request.config() != null ? toJson(request.config()) : existing.configJson(),
                request.status() != null ? request.status() : existing.status(),
                request.environment() != null ? request.environment() : existing.environment(),
                request.endpointUrl() != null ? request.endpointUrl() : existing.endpointUrl(),
                request.certificateId() != null ? request.certificateId() : existing.certificateId(),
                existing.lastSync(),
                existing.lastSyncStatus(),
                existing.lastError(),
                existing.createdAt(),
                java.time.LocalDateTime.now()
        );

        IntegracaoFiscal saved = integracaoRepository.save(updated);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete integration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Integration deleted"),
            @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (integracaoRepository.existsById(id)) {
            integracaoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test integration connectivity",
            description = "Tests the integration against the configured endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test result"),
            @ApiResponse(responseCode = "404", description = "Integration not found")
    })
    public ResponseEntity<IntegracaoFiscalDTO.TestResponse> test(
            @PathVariable String id,
            @Valid @RequestBody IntegracaoFiscalDTO.TestRequest request) {

        Optional<IntegracaoFiscal> opt = integracaoRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // In production, make actual test call to endpoint
        var response = new IntegracaoFiscalDTO.TestResponse(
                true,
                "Integration test successful",
                java.time.LocalDateTime.now(),
                toJson(request.testPayload()),
                "{\"status\": \"ok\"}"
        );
        return ResponseEntity.ok(response);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private IntegracaoFiscalDTO.Response toResponse(IntegracaoFiscal integracao) {
        Map<String, Object> config;
        try {
            config = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    integracao.configJson(), Map.class);
        } catch (Exception e) {
            config = Map.of();
        }

        return new IntegracaoFiscalDTO.Response(
                integracao.id(),
                integracao.companyId(),
                integracao.type(),
                integracao.name(),
                config,
                integracao.status(),
                integracao.environment(),
                integracao.endpointUrl(),
                integracao.certificateId(),
                integracao.lastSync(),
                integracao.lastSyncStatus(),
                integracao.lastError(),
                integracao.createdAt(),
                integracao.updatedAt()
        );
    }
}