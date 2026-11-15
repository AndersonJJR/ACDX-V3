package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.application.usecase.RecalculateValuationUseCase;
import com.erp.brazil.inventory.domain.model.RepostingJob;
import com.erp.brazil.inventory.domain.repository.RepostingJobRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reposting")
@RequiredArgsConstructor
@Tag(name = "Reposting", description = "Valuation reposting APIs")
public class RepostingController {

    private final RepostingJobRepository jobRepository;
    private final RecalculateValuationUseCase recalculateValuationUseCase;

    @PostMapping
    @Operation(summary = "Create a new reposting job")
    public ResponseEntity<RepostingJob> createRepostingJob(@RequestBody RepostingJob job) {
        job.setStatus(RepostingJob.RepostingStatus.PENDING);
        RepostingJob saved = jobRepository.save(job);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reposting job by ID")
    public ResponseEntity<RepostingJob> getRepostingJob(@PathVariable UUID id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List reposting jobs")
    public ResponseEntity<Page<RepostingJob>> listRepostingJobs(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String status,
            Pageable pageable) {

        Page<RepostingJob> jobs;
        if (status != null) {
            jobs = jobRepository.findByCompanyIdAndStatus(companyId, RepostingJob.RepostingStatus.valueOf(status), pageable);
        } else {
            jobs = Page.empty(pageable);
        }
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute reposting job")
    public ResponseEntity<RepostingJob> executeRepostingJob(@PathVariable UUID id) {
        RepostingJob job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reposting job not found: " + id));

        job.setStatus(RepostingJob.RepostingStatus.QUEUED);
        jobRepository.save(job);

        // In production, this would be sent to a message queue
        // For now, execute synchronously (not recommended for large jobs)
        recalculateValuationUseCase.recalculateValuation(job.getCompanyId(), id);

        return ResponseEntity.ok(job);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel reposting job")
    public ResponseEntity<RepostingJob> cancelRepostingJob(@PathVariable UUID id) {
        return jobRepository.findById(id)
                .map(job -> {
                    job.setStatus(RepostingJob.RepostingStatus.CANCELLED);
                    return ResponseEntity.ok(jobRepository.save(job));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}