package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.application.usecase.LockFiscalPeriodUseCase;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.FiscalYear;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.FiscalYearRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal-periods")
@Tag(name = "Fiscal Periods", description = "Fiscal Period management APIs")
public class FiscalPeriodController {

    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final LockFiscalPeriodUseCase lockFiscalPeriodUseCase;

    public FiscalPeriodController(FiscalPeriodRepository fiscalPeriodRepository,
                                  FiscalYearRepository fiscalYearRepository,
                                  LockFiscalPeriodUseCase lockFiscalPeriodUseCase) {
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.fiscalYearRepository = fiscalYearRepository;
        this.lockFiscalPeriodUseCase = lockFiscalPeriodUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new fiscal period")
    public ResponseEntity<FiscalPeriodResponse> create(@Valid @RequestBody CreateFiscalPeriodRequest request) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(request.fiscalYearId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + request.fiscalYearId()));

        FiscalPeriod period = new FiscalPeriod(
                fiscalYear,
                request.periodNumber(),
                request.startDate(),
                request.endDate()
        );
        period.setName(request.name());

        FiscalPeriod saved = fiscalPeriodRepository.save(period);
        return ResponseEntity.created(URI.create("/api/v1/fiscal-periods/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fiscal period by ID")
    public ResponseEntity<FiscalPeriodResponse> getById(@PathVariable UUID id) {
        return fiscalPeriodRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List fiscal periods")
    public ResponseEntity<Page<FiscalPeriodResponse>> list(
            @Parameter(description = "Fiscal year ID") @RequestParam(required = false) UUID fiscalYearId,
            @Parameter(description = "Locked status") @RequestParam(required = false) Boolean locked,
            @Parameter(description = "Closed status") @RequestParam(required = false) Boolean closed,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<FiscalPeriod> periods;

        if (fiscalYearId != null) {
            FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId).orElseThrow();
            periods = fiscalPeriodRepository.findByFiscalYear(fiscalYear, pageable);
        } else if (Boolean.TRUE.equals(locked)) {
            periods = fiscalPeriodRepository.findByLockedFalse(pageable); // Would need custom query
        } else {
            periods = fiscalPeriodRepository.findAll(pageable);
        }

        return ResponseEntity.ok(periods.map(this::toResponse));
    }

    @GetMapping("/by-date")
    @Operation(summary = "Get fiscal period by date")
    public ResponseEntity<FiscalPeriodResponse> getByDate(
            @Parameter(description = "Fiscal year ID") @RequestParam UUID fiscalYearId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId).orElseThrow();
        return fiscalPeriodRepository.findByFiscalYearAndDate(fiscalYear, date)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock fiscal period")
    public ResponseEntity<LockFiscalPeriodUseCase.Result> lock(
            @PathVariable UUID id,
            @RequestParam String lockedBy) {
        var result = lockFiscalPeriodUseCase.execute(id, lockedBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock fiscal period")
    public ResponseEntity<LockFiscalPeriodUseCase.Result> unlock(
            @PathVariable UUID id,
            @RequestParam String unlockedBy) {
        var result = lockFiscalPeriodUseCase.unlock(id, unlockedBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update fiscal period")
    public ResponseEntity<FiscalPeriodResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateFiscalPeriodRequest request) {
        FiscalPeriod period = fiscalPeriodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + id));

        if (period.isLocked() || period.isClosed()) {
            throw new IllegalStateException("Cannot modify locked or closed period");
        }

        period.setName(request.name());
        period.setStartDate(request.startDate());
        period.setEndDate(request.endDate());

        FiscalPeriod saved = fiscalPeriodRepository.save(period);
        return ResponseEntity.ok(toResponse(saved));
    }

    private FiscalPeriodResponse toResponse(FiscalPeriod period) {
        return new FiscalPeriodResponse(
                period.getId(),
                period.getFiscalYear().getId(),
                period.getFiscalYear().getYear(),
                period.getPeriodNumber(),
                period.getName(),
                period.getStartDate(),
                period.getEndDate(),
                period.isLocked(),
                period.isClosed(),
                period.getLockedAt(),
                period.getLockedBy(),
                period.getClosedAt(),
                period.getCreatedAt(),
                period.getUpdatedAt()
        );
    }

    // Request/Response Records
    public record CreateFiscalPeriodRequest(
            @NotNull UUID fiscalYearId,
            @NotNull Integer periodNumber,
            String name,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {}

    public record UpdateFiscalPeriodRequest(
            String name,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {}

    public record FiscalPeriodResponse(
            UUID id,
            UUID fiscalYearId,
            Integer fiscalYear,
            Integer periodNumber,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean locked,
            boolean closed,
            LocalDate lockedAt,
            String lockedBy,
            LocalDate closedAt,
            java.time.LocalDate createdAt,
            java.time.LocalDate updatedAt
    ) {}
}