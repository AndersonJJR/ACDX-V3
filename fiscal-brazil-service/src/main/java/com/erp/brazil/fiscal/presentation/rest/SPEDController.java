package com.erp.brazil.fiscal.presentation.rest;

import com.erp.brazil.fiscal.application.dto.SPEDDTO;
import com.erp.brazil.fiscal.application.usecase.GenerateSPEDUseCase;
import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.repository.SPEDRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for SPED generation operations.
 */
@RestController
@RequestMapping("/api/v1/sped")
@Tag(name = "SPED", description = "Sistema Público de Escrituração Digital generation (ECD, ECF, EFD-ICMS-IPI, EFD-Contribuições)")
public class SPEDController {

    private final GenerateSPEDUseCase generateUseCase;
    private final SPEDRepository spedRepository;

    public SPEDController(GenerateSPEDUseCase generateUseCase, SPEDRepository spedRepository) {
        this.generateUseCase = generateUseCase;
        this.spedRepository = spedRepository;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate SPED file",
            description = "Generates a SPED file (ECD, ECF, EFD-ICMS-IPI, or EFD-Contribuições) for the given period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Generation started",
                    content = @Content(schema = @Schema(implementation = SPEDDTO.GenerateResponse.class))),
            @ApiResponse(responseCode = "200", description = "Generation completed (sync mode)"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SPEDDTO.GenerateResponse> generate(
            @Valid @RequestBody SPEDDTO.GenerateRequest request) {

        if (request.async()) {
            CompletableFuture<SPEDDTO.GenerateResponse> future = generateUseCase.executeAsync(request);
            return ResponseEntity.accepted().build();
        } else {
            SPEDDTO.GenerateResponse response = generateUseCase.executeSync(request);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SPED generation status",
            description = "Returns the status and details of a SPED generation job.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SPED details",
                    content = @Content(schema = @Schema(implementation = SPEDDTO.QueryResponse.class))),
            @ApiResponse(responseCode = "404", description = "SPED not found")
    })
    public ResponseEntity<SPEDDTO.QueryResponse> getById(@PathVariable String id) {
        Optional<SPEDBlock> spedOpt = spedRepository.findById(id);
        if (spedOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SPEDBlock sped = spedOpt.get();
        var response = new SPEDDTO.QueryResponse(
                sped,
                sped.getStatus(),
                "VALIDATION_RESULT_PLACEHOLDER",
                sped.getGeneratedAt()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download SPED file",
            description = "Downloads the generated SPED file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File content"),
            @ApiResponse(responseCode = "404", description = "SPED or file not found")
    })
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        Optional<SPEDBlock> spedOpt = spedRepository.findById(id);
        if (spedOpt.isEmpty() || spedOpt.get().getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        SPEDBlock sped = spedOpt.get();
        Path filePath = Paths.get(sped.getFilePath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = Files.readAllBytes(filePath);
        ByteArrayResource resource = new ByteArrayResource(content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", filePath.getFileName().toString());
        headers.setContentLength(content.length);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping
    @Operation(summary = "List SPED generations",
            description = "Lists SPED generations with pagination and filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of SPED generations")
    })
    public ResponseEntity<SPEDDTO.ListResponse> list(
            @RequestParam String companyId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<SPEDBlock> speds;
        if (period != null) {
            speds = spedRepository.findByCompanyIdAndPeriod(companyId, period);
        } else {
            speds = spedRepository.findByCompanyId(companyId);
        }

        // Filter by type and status if provided
        if (type != null) {
            speds = speds.stream().filter(s -> s.getClass().getSimpleName().equalsIgnoreCase(type)).toList();
        }
        if (status != null) {
            speds = speds.stream().filter(s -> s.getStatus().name().equalsIgnoreCase(status)).toList();
        }

        int from = Math.min(page * size, speds.size());
        int to = Math.min(from + size, speds.size());

        var response = new SPEDDTO.ListResponse(
                speds.subList(from, to),
                speds.size(),
                page,
                size
        );
        return ResponseEntity.ok(response);
    }
}