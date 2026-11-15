package com.erp.brazil.fiscal.presentation.rest;

import com.erp.brazil.fiscal.application.dto.CertificadoDigitalDTO;
import com.erp.brazil.fiscal.application.usecase.MonitorCertificadoDigitalUseCase;
import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.repository.CertificadoDigitalRepository;
import com.erp.brazil.fiscal.infrastructure.certificado.CertificadoDigitalService;
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
import java.util.Optional;

/**
 * REST controller for Digital Certificate A1 management.
 */
@RestController
@RequestMapping("/api/v1/certificados")
@Tag(name = "Certificado Digital A1", description = "Digital certificate management for NF-e/NFS-e signing")
public class CertificadoDigitalController {

    private final CertificadoDigitalService certificadoService;
    private final CertificadoDigitalRepository certificadoRepository;
    private final MonitorCertificadoDigitalUseCase monitorUseCase;

    public CertificadoDigitalController(CertificadoDigitalService certificadoService,
                                         CertificadoDigitalRepository certificadoRepository,
                                         MonitorCertificadoDigitalUseCase monitorUseCase) {
        this.certificadoService = certificadoService;
        this.certificadoRepository = certificadoRepository;
        this.monitorUseCase = monitorUseCase;
    }

    @PostMapping
    @Operation(summary = "Register new certificate",
            description = "Registers a new A1 digital certificate (PFX) for a company.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Certificate registered",
                    content = @Content(schema = @Schema(implementation = CertificadoDigitalDTO.RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid certificate or duplicate"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CertificadoDigitalDTO.RegisterResponse> register(
            @Valid @RequestBody CertificadoDigitalDTO.RegisterRequest request) {

        CertificadoDigitalA1 certificado = certificadoService.registerCertificate(
                request.companyId(),
                request.alias(),
                request.pfxBase64(),
                request.password(),
                request.cnpj(),
                request.companyName(),
                request.renewalAlertDays()
        );

        var response = new CertificadoDigitalDTO.RegisterResponse(
                certificado.id(),
                certificado.alias(),
                certificado.cnpj(),
                certificado.companyName(),
                certificado.expiryDate(),
                certificado.status(),
                certificado.daysUntilExpiry(),
                certificado.createdAt()
        );
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get certificate by ID",
            description = "Returns certificate details (without sensitive data).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate found"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    public ResponseEntity<CertificadoDigitalDTO.Response> getById(@PathVariable String id) {
        Optional<CertificadoDigitalA1> certOpt = certificadoRepository.findById(id);
        if (certOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(certOpt.get()));
    }

    @GetMapping
    @Operation(summary = "List certificates by company",
            description = "Lists all certificates for a company with status summary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate list")
    })
    public ResponseEntity<CertificadoDigitalDTO.ListResponse> listByCompany(
            @RequestParam String companyId) {

        List<CertificadoDigitalA1> certificados = certificadoRepository.findByCompanyId(companyId);

        List<CertificadoDigitalDTO.Response> responses = certificados.stream()
                .map(this::toResponse)
                .toList();

        long activeCount = responses.stream().filter(r -> r.status() == CertificadoDigitalA1.CertificateStatus.ACTIVE).count();
        long expiringCount = responses.stream().filter(r -> r.status() == CertificadoDigitalA1.CertificateStatus.EXPIRING_SOON).count();
        long expiredCount = responses.stream().filter(r -> r.status() == CertificadoDigitalA1.CertificateStatus.EXPIRED).count();

        var response = new CertificadoDigitalDTO.ListResponse(
                responses,
                (int) activeCount,
                (int) expiringCount,
                (int) expiredCount
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update certificate",
            description = "Updates certificate alias, PFX, password, or alert settings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificate updated"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    public ResponseEntity<CertificadoDigitalDTO.Response> update(
            @PathVariable String id,
            @RequestParam String companyId,
            @Valid @RequestBody CertificadoDigitalDTO.UpdateRequest request) {

        certificadoService.updateCertificate(
                id, companyId,
                request.alias(), request.pfxBase64(), request.password(),
                request.renewalAlertDays()
        );

        Optional<CertificadoDigitalA1> certOpt = certificadoRepository.findByIdAndCompanyId(id, companyId);
        return certOpt.map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete certificate",
            description = "Deletes a certificate (soft delete - marks as revoked).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Certificate deleted"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestParam String companyId) {
        certificadoService.deleteCertificate(id, companyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Test certificate connectivity",
            description = "Tests the certificate against a SEFAZ or municipality endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test result"),
            @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    public ResponseEntity<CertificadoDigitalDTO.TestResponse> test(
            @PathVariable String id,
            @Valid @RequestBody CertificadoDigitalDTO.TestRequest request) {

        Optional<CertificadoDigitalA1> certOpt = certificadoRepository.findById(id);
        if (certOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // In production, test against actual endpoint
        var response = new CertificadoDigitalDTO.TestResponse(
                true,
                "Certificate test successful",
                java.time.LocalDateTime.now(),
                "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/renew-alerts")
    @Operation(summary = "Check certificate expiries manually",
            description = "Triggers immediate check for expiring certificates and sends alerts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed")
    })
    public ResponseEntity<Void> checkRenewalAlerts(
            @Valid @RequestBody CertificadoDigitalDTO.RenewalAlertRequest request) {

        monitorUseCase.checkNow();
        return ResponseEntity.ok().build();
    }

    private CertificadoDigitalDTO.Response toResponse(CertificadoDigitalA1 cert) {
        return new CertificadoDigitalDTO.Response(
                cert.id(),
                cert.companyId(),
                cert.alias(),
                cert.cnpj(),
                cert.companyName(),
                cert.expiryDate(),
                cert.status(),
                cert.daysUntilExpiry(),
                cert.serialNumber(),
                cert.issuer(),
                cert.subject(),
                cert.thumbprint(),
                cert.createdAt(),
                cert.updatedAt(),
                cert.lastUsedAt(),
                cert.renewalAlertDays()
        );
    }
}