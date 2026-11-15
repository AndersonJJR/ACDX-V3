package com.erp.brazil.fiscal.application.dto;

import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.model.SPEDStatus;
import com.erp.brazil.fiscal.domain.model.SPEDType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for SPED generation API.
 */
public class SPEDDTO {

    @Schema(description = "SPED generation request")
    public record GenerateRequest(
            @NotBlank String companyId,
            @NotNull SPEDType type,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            String profile,
            String layout,
            boolean async,
            String callbackUrl
    ) {}

    @Schema(description = "SPED generation response")
    public record GenerateResponse(
            String spedId,
            SPEDStatus status,
            String message,
            java.time.LocalDateTime generatedAt,
            String filePath,
            String fileHash
    ) {}

    @Schema(description = "SPED query response")
    public record QueryResponse(
            SPEDBlock sped,
            SPEDStatus status,
            String validationResult,
            java.time.LocalDateTime lastValidation
    ) {}

    @Schema(description = "SPED list response")
    public record ListResponse(
            List<SPEDBlock> speds,
            long total,
            int page,
            int size
    ) {}

    @Schema(description = "SPED download response")
    public record DownloadResponse(
            String fileName,
            String contentType,
            byte[] content,
            String fileHash
    ) {}
}