package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.application.usecase.RecalculateValuationUseCase;
import com.erp.brazil.inventory.domain.model.RepostingJob;
import com.erp.brazil.inventory.domain.repository.RepostingJobRepository;
import com.erp.brazil.inventory.presentation.rest.dto.RepostingJobResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reposting")
@RequiredArgsConstructor
@Tag(name = "Reposting", description = "Stock valuation recalculation (reposting) jobs")
public class RepostingController {

    private final RepostingJobRepository repostingJobRepository;
    private final RecalculateValuationUseCase recalculateValuationUseCase;

    @PostMapping("/trigger")
    @Operation(summary = "Trigger a new reposting job")
    public ResponseEntity<RepostingJobResponse> triggerReposting(@RequestBody RepostingRequest request) {
        RepostingJob job = RepostingJob.builder()
                .companyId(request.companyId())
                .jobName(request.jobName())
                .postingDateFrom(request.postingDateFrom())
                .postingDateTo(request.postingDateTo())
                .itemGroup(request.itemGroup())
                .itemId(request.itemId())
                .warehouseId(request.warehouseId())
                .status(RepostingJob.RepostingStatus.PENDING)
                .priority(request.priority() != null ? request.priority() : 5)
                .chunkSize(request.chunkSize() != null ? request.chunkSize() : 1000)
                .isDryRun(request.isDryRun() != null ? request.isDryRun() : false)
                .triggeredBy(request.triggeredBy())
                .build();

        RepostingJob saved = repostingJobRepository.save(job);

        // Trigger async processing via message queue
        // In a real implementation, you'd publish a message to the queue
        // For now, we'll just return the job

        return ResponseEntity.ok(mapToResponse(saved));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get reposting job status")
    public ResponseEntity<RepostingJobResponse> getJobStatus(@Parameter(description = "Job ID") @PathVariable UUID id) {
        return repostingJobRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    @Operation(summary = "Get reposting job history")
    public ResponseEntity<Page<RepostingJobResponse>> getHistory(
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<RepostingJob> jobs;
        if (status != null) {
            jobs = repostingJobRepository.findByCompanyIdAndStatus(
                    companyId, RepostingJob.RepostingStatus.valueOf(status), pageable);
        } else {
            jobs = repostingJobRepository.findAll(pageable); // Would need company filter
        }

        return ResponseEntity.ok(jobs.map(this::mapToResponse));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending reposting jobs")
    public ResponseEntity<List<RepostingJobResponse>> getPendingJobs(
            @Parameter(description = "Company ID") @RequestParam UUID companyId) {

        List<RepostingJob> jobs = repostingJobRepository.findPendingJobs(companyId);
        return ResponseEntity.ok(jobs.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    private RepostingJobResponse mapToResponse(RepostingJob job) {
        return RepostingJobResponse.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .jobName(job.getJobName())
                .postingDateFrom(job.getPostingDateFrom())
                .postingDateTo(job.getPostingDateTo())
                .itemGroup(job.getItemGroup())
                .itemId(job.getItemId())
                .warehouseId(job.getWarehouseId())
                .status(job.getStatus().name())
                .priority(job.getPriority())
                .chunkSize(job.getChunkSize())
                .totalRecords(job.getTotalRecords())
                .processedRecords(job.getProcessedRecords())
                .failedRecords(job.getFailedRecords())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .errorMessage(job.getErrorMessage())
                .isDryRun(job.getIsDryRun())
                .triggeredBy(job.getTriggeredBy())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    public record RepostingRequest(
            UUID companyId,
            String jobName,
            LocalDate postingDateFrom,
            LocalDate postingDateTo,
            String itemGroup,
            UUID itemId,
            UUID warehouseId,
            Integer priority,
            Integer chunkSize,
            Boolean isDryRun,
            UUID triggeredBy
    ) {}
}