package com.erp.brazil.fiscal.presentation.rest;

import com.erp.brazil.fiscal.application.dto.NFEDTO;
import com.erp.brazil.fiscal.application.usecase.CancelNFEUseCase;
import com.erp.brazil.fiscal.application.usecase.EmitNFEUseCase;
import com.erp.brazil.fiscal.application.usecase.InutilizeNFEUseCase;
import com.erp.brazil.fiscal.application.usecase.ValidateNFEUseCase;
import com.erp.brazil.fiscal.domain.model.NFE;
import com.erp.brazil.fiscal.domain.repository.NFERepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for NF-e operations.
 */
@RestController
@RequestMapping("/api/v1/nfe")
@Tag(name = "NF-e", description = "Nota Fiscal Eletrônica operations (emit, cancel, inutilize, query)")
public class NFEController {

    private final EmitNFEUseCase emitUseCase;
    private final CancelNFEUseCase cancelUseCase;
    private final InutilizeNFEUseCase inutilizeUseCase;
    private final ValidateNFEUseCase validateUseCase;
    private final NFERepository nfeRepository;

    public NFEController(EmitNFEUseCase emitUseCase,
                         CancelNFEUseCase cancelUseCase,
                         InutilizeNFEUseCase inutilizeUseCase,
                         ValidateNFEUseCase validateUseCase,
                         NFERepository nfeRepository) {
        this.emitUseCase = emitUseCase;
        this.cancelUseCase = cancelUseCase;
        this.inutilizeUseCase = inutilizeUseCase;
        this.validateUseCase = validateUseCase;
        this.nfeRepository = nfeRepository;
    }

    @PostMapping("/emit")
    @Operation(summary = "Emit NF-e",
            description = "Emits an NF-e asynchronously. Returns immediately with a tracking ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "NF-e accepted for processing",
                    content = @Content(schema = @Schema(implementation = NFEDTO.EmitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NFEDTO.EmitResponse> emit(
            @Valid @RequestBody NFEDTO.EmitRequest request) {

        if (request.async()) {
            CompletableFuture<NFEDTO.EmitResponse> future = emitUseCase.executeAsync(request);
            // Return immediately with PENDING status
            return ResponseEntity.accepted().build();
        } else {
            NFEDTO.EmitResponse response = emitUseCase.executeSync(request);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/emit/sync")
    @Operation(summary = "Emit NF-e synchronously",
            description = "Emits an NF-e and waits for SEFAZ response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "NF-e processed",
                    content = @Content(schema = @Schema(implementation = NFEDTO.EmitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NFEDTO.EmitResponse> emitSync(
            @Valid @RequestBody NFEDTO.EmitRequest request) {

        NFEDTO.EmitResponse response = emitUseCase.executeSync(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel/{accessKey}")
    @Operation(summary = "Cancel NF-e",
            description = "Cancels an authorized NF-e within 24 hours of emission.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "NF-e cancelled",
                    content = @Content(schema = @Schema(implementation = NFEDTO.CancelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cancellation not allowed"),
            @ApiResponse(responseCode = "404", description = "NF-e not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NFEDTO.CancelResponse> cancel(
            @PathVariable String accessKey,
            @Valid @RequestBody NFEDTO.CancelRequest request) {

        // Ensure access key matches path
        var cancelRequest = new NFEDTO.CancelRequest(accessKey, request.justification(), request.protocol());
        NFEDTO.CancelResponse response = cancelUseCase.execute(cancelRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/inutilize")
    @Operation(summary = "Inutilize NF-e number range",
            description = "Inutilizes a range of NF-e numbers that will not be used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Range inutilized",
                    content = @Content(schema = @Schema(implementation = NFEDTO.InutilizeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Range already in use"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<NFEDTO.InutilizeResponse> inutilize(
            @Valid @RequestBody NFEDTO.InutilizeRequest request) {

        NFEDTO.InutilizeResponse response = inutilizeUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accessKey}")
    @Operation(summary = "Get NF-e by access key",
            description = "Returns the NF-e details including status and SEFAZ response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "NF-e found",
                    content = @Content(schema = @Schema(implementation = NFEDTO.QueryResponse.class))),
            @ApiResponse(responseCode = "404", description = "NF-e not found")
    })
    public ResponseEntity<NFEDTO.QueryResponse> getByAccessKey(@PathVariable String accessKey) {
        Optional<NFE> nfeOpt = nfeRepository.findByAccessKey(accessKey);
        if (nfeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        NFE nfe = nfeOpt.get();
        var response = new NFEDTO.QueryResponse(
                nfe,
                nfe.status(),
                nfe.protocol(),
                nfe.updatedAt(),
                List.of() // Events would be fetched from event store
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accessKey}/xml")
    @Operation(summary = "Get NF-e XML",
            description = "Returns the signed XML of the NF-e.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "XML content"),
            @ApiResponse(responseCode = "404", description = "NF-e not found")
    })
    public ResponseEntity<String> getXml(@PathVariable String accessKey) {
        Optional<NFE> nfeOpt = nfeRepository.findByAccessKey(accessKey);
        if (nfeOpt.isEmpty() || nfeOpt.get().xmlContent() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment", "nfe-" + accessKey + ".xml");

        return ResponseEntity.ok().headers(headers).body(nfeOpt.get().xmlContent());
    }

    @GetMapping("/{accessKey}/pdf")
    @Operation(summary = "Get NF-e PDF (DANFE)",
            description = "Returns the PDF representation of the NF-e (DANFE).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF content"),
            @ApiResponse(responseCode = "404", description = "NF-e or PDF not found")
    })
    public ResponseEntity<byte[]> getPdf(@PathVariable String accessKey) {
        Optional<NFE> nfeOpt = nfeRepository.findByAccessKey(accessKey);
        if (nfeOpt.isEmpty() || nfeOpt.get().pdfContent() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "danfe-" + accessKey + ".pdf");

        // In production, decode base64 PDF content
        byte[] pdfBytes = nfeOpt.get().pdfContent().getBytes();
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate NF-e XML",
            description = "Validates NF-e XML against XSD schema and business rules.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation result"),
            @ApiResponse(responseCode = "400", description = "Invalid XML")
    })
    public ResponseEntity<ValidateNFEUseCase.ValidationResult> validate(
            @RequestBody String xmlContent) {

        var result = validateUseCase.validate(xmlContent);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "List NF-es",
            description = "Lists NF-es with pagination and filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of NF-es")
    })
    public ResponseEntity<NFEDTO.ListResponse> list(
            @RequestParam String companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<NFE> nfes;
        if (startDate != null && endDate != null) {
            nfes = nfeRepository.findByCompanyIdAndEmissionDateBetween(
                    companyId,
                    java.time.LocalDateTime.parse(startDate),
                    java.time.LocalDateTime.parse(endDate)
            );
        } else if (status != null) {
            nfes = nfeRepository.findByCompanyIdAndStatus(companyId, status);
        } else {
            nfes = nfeRepository.findByCompanyIdOrderByEmissionDateDesc(companyId);
        }

        // Apply pagination manually (in production, use Pageable)
        int from = Math.min(page * size, nfes.size());
        int to = Math.min(from + size, nfes.size());

        var response = new NFEDTO.ListResponse(
                nfes.subList(from, to),
                nfes.size(),
                page,
                size
        );
        return ResponseEntity.ok(response);
    }
}