package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.application.dto.SPEDDTO;
import com.erp.brazil.fiscal.domain.model.SPEDBlock;
import com.erp.brazil.fiscal.domain.repository.NFERepository;
import com.erp.brazil.fiscal.domain.repository.SPEDRepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import com.erp.brazil.fiscal.infrastructure.sped.SPEDWriter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Use case for generating SPED files (ECD, ECF, EFD-ICMS-IPI, EFD-Contribuições).
 */
@Service
@Transactional
public class GenerateSPEDUseCase {

    private final SPEDRepository spedRepository;
    private final NFERepository nfeRepository;
    private final SPEDWriter spedWriter;
    private final EventPublisher eventPublisher;
    private final String outputDirectory;

    public GenerateSPEDUseCase(SPEDRepository spedRepository,
                               NFERepository nfeRepository,
                               SPEDWriter spedWriter,
                               EventPublisher eventPublisher,
                               SPEDProperties properties) {
        this.spedRepository = spedRepository;
        this.nfeRepository = nfeRepository;
        this.spedWriter = spedWriter;
        this.eventPublisher = eventPublisher;
        this.outputDirectory = properties.getOutputDirectory();
    }

    @Async("spedExecutor")
    public CompletableFuture<SPEDDTO.GenerateResponse> executeAsync(SPEDDTO.GenerateRequest request) {
        return CompletableFuture.supplyAsync(() -> executeSync(request));
    }

    public SPEDDTO.GenerateResponse executeSync(SPEDDTO.GenerateRequest request) {
        String spedId = UUID.randomUUID().toString();
        String period = request.startDate().getYear() + String.format("%02d", request.startDate().getMonthValue());

        // 1. Create SPED record with PENDING status
        SPEDBlock sped = createSpedRecord(spedId, request, period, SPEDBlock.SPEDStatus.GENERATING);
        spedRepository.save(sped);

        try {
            // 2. Fetch NF-es for the period
            List<NFE> nfes = nfeRepository.findByCompanyIdAndEmissionDateBetween(
                    request.companyId(),
                    request.startDate().atStartOfDay(),
                    request.endDate().atTime(23, 59, 59)
            );

            // 3. Generate SPED file based on type
            String filePath;
            String fileHash;

            switch (request.type()) {
                case ECD -> {
                    filePath = spedWriter.writeECD(spedId, request.companyId(), nfes, request.startDate(), request.endDate(), outputDirectory);
                    fileHash = calculateHash(filePath);
                }
                case ECF -> {
                    filePath = spedWriter.writeECF(spedId, request.companyId(), nfes, request.startDate(), request.endDate(), outputDirectory);
                    fileHash = calculateHash(filePath);
                }
                case EFD_ICMS_IPI -> {
                    filePath = spedWriter.writeEFD_ICMS_IPI(spedId, request.companyId(), nfes, request.startDate(), request.endDate(), outputDirectory);
                    fileHash = calculateHash(filePath);
                }
                case EFD_CONTRIBUICOES -> {
                    filePath = spedWriter.writeEFD_Contribuicoes(spedId, request.companyId(), nfes, request.startDate(), request.endDate(), outputDirectory);
                    fileHash = calculateHash(filePath);
                }
                default -> throw new IllegalArgumentException("Unsupported SPED type: " + request.type());
            }

            // 4. Update SPED record with success
            SPEDBlock completed = new SPEDBlock.EFD_ICMS_IPI(
                    spedId, request.companyId(), period, request.startDate(), request.endDate(),
                    SPEDBlock.SPEDStatus.GENERATED, LocalDateTime.now(),
                    filePath, fileHash, "1.0", request.profile() != null ? request.profile() : "A", List.of()
            );
            spedRepository.save(completed);

            // 5. Publish event
            var event = new EventDTO.SPEDGeneratedEvent(
                    UUID.randomUUID().toString(),
                    spedId,
                    request.companyId(),
                    request.type().name(),
                    period,
                    "GENERATED",
                    filePath,
                    fileHash,
                    LocalDateTime.now()
            );
            eventPublisher.publish(event);

            return new SPEDDTO.GenerateResponse(
                    spedId, SPEDBlock.SPEDStatus.GENERATED, "SPED generated successfully",
                    LocalDateTime.now(), filePath, fileHash
            );

        } catch (Exception e) {
            // 6. Update SPED record with error
            SPEDBlock failed = new SPEDBlock.EFD_ICMS_IPI(
                    spedId, request.companyId(), period, request.startDate(), request.endDate(),
                    SPEDBlock.SPEDStatus.ERROR, LocalDateTime.now(),
                    null, null, "1.0", request.profile() != null ? request.profile() : "A", List.of()
            );
            spedRepository.save(failed);

            return new SPEDDTO.GenerateResponse(
                    spedId, SPEDBlock.SPEDStatus.ERROR, "Generation failed: " + e.getMessage(),
                    LocalDateTime.now(), null, null
            );
        }
    }

    private SPEDBlock createSpedRecord(String id, SPEDDTO.GenerateRequest request, String period, SPEDBlock.SPEDStatus status) {
        return switch (request.type()) {
            case ECD -> new SPEDBlock.ECD(id, request.companyId(), period, request.startDate(), request.endDate(),
                    status, LocalDateTime.now(), null, null, "1.0", SPEDBlock.ECD.Layout.LAYOUT_1, List.of());
            case ECF -> new SPEDBlock.ECF(id, request.companyId(), period, request.startDate(), request.endDate(),
                    status, LocalDateTime.now(), null, null, "1.0", "1", List.of());
            case EFD_ICMS_IPI -> new SPEDBlock.EFD_ICMS_IPI(id, request.companyId(), period, request.startDate(), request.endDate(),
                    status, LocalDateTime.now(), null, null, "1.0", request.profile() != null ? request.profile() : "A", List.of());
            case EFD_CONTRIBUICOES -> new SPEDBlock.EFD_Contribuicoes(id, request.companyId(), period, request.startDate(), request.endDate(),
                    status, LocalDateTime.now(), null, null, "1.0", request.profile() != null ? request.profile() : "A", List.of());
        };
    }

    private String calculateHash(String filePath) throws IOException {
        var bytes = Files.readAllBytes(Paths.get(filePath));
        var digest = java.security.MessageDigest.getInstance("SHA-256");
        var hash = digest.digest(bytes);
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public record SPEDProperties(String outputDirectory) {}
}