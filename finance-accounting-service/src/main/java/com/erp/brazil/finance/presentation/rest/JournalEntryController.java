package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.application.usecase.PostJournalEntryUseCase;
import com.erp.brazil.finance.application.usecase.ReverseJournalEntryUseCase;
import com.erp.brazil.finance.domain.model.JournalEntry;
import com.erp.brazil.finance.domain.model.JournalEntryLine;
import com.erp.brazil.finance.domain.model.JournalEntryStatus;
import com.erp.brazil.finance.domain.repository.JournalEntryRepository;
import com.erp.brazil.finance.domain.repository.JournalEntryLineRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal-entries")
@Tag(name = "Journal Entries", description = "Journal Entry management APIs")
public class JournalEntryController {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final PostJournalEntryUseCase postJournalEntryUseCase;
    private final ReverseJournalEntryUseCase reverseJournalEntryUseCase;

    public JournalEntryController(JournalEntryRepository journalEntryRepository,
                                  JournalEntryLineRepository journalEntryLineRepository,
                                  PostJournalEntryUseCase postJournalEntryUseCase,
                                  ReverseJournalEntryUseCase reverseJournalEntryUseCase) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
        this.reverseJournalEntryUseCase = reverseJournalEntryUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new journal entry")
    public ResponseEntity<JournalEntryResponse> create(@Valid @RequestBody CreateJournalEntryRequest request) {
        JournalEntry journalEntry = new JournalEntry(
                request.reference(),
                request.entryDate(),
                null, // fiscalPeriod will be set by service
                request.description()
        );

        // Set fiscal period from request or derive from date
        // For now, assume it's provided
        // journalEntry.setFiscalPeriod(...)

        for (CreateJournalEntryLineRequest lineReq : request.lines()) {
            journalEntry.addLine(new JournalEntryLine(
                    journalEntry,
                    null, // account will be resolved
                    lineReq.lineNumber(),
                    lineReq.debitAmount(),
                    lineReq.creditAmount()
            ));
            journalEntry.getLines().getLast().setDescription(lineReq.description());
            // journalEntry.getLines().getLast().setCostCenter(...)
        }

        JournalEntry saved = journalEntryRepository.save(journalEntry);
        return ResponseEntity.created(URI.create("/api/v1/journal-entries/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get journal entry by ID")
    public ResponseEntity<JournalEntryResponse> getById(@PathVariable UUID id) {
        return journalEntryRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List journal entries with filters")
    public ResponseEntity<Page<JournalEntryResponse>> list(
            @Parameter(description = "Fiscal period ID") @RequestParam(required = false) UUID fiscalPeriodId,
            @Parameter(description = "Status filter") @RequestParam(required = false) JournalEntryStatus status,
            @Parameter(description = "Entry date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Entry date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Reference search") @RequestParam(required = false) String reference,
            @PageableDefault(size = 20) Pageable pageable) {
        // Implementation would use repository with dynamic query
        Page<JournalEntry> entries = journalEntryRepository.findAll(pageable);
        return ResponseEntity.ok(entries.map(this::toResponse));
    }

    @PostMapping("/{id}/post")
    @Operation(summary = "Post a draft journal entry")
    public ResponseEntity<PostJournalEntryUseCase.Result> post(
            @PathVariable UUID id,
            @RequestParam String postedBy) {
        var result = postJournalEntryUseCase.execute(id, postedBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse a posted journal entry")
    public ResponseEntity<ReverseJournalEntryUseCase.Result> reverse(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reversalDate,
            @RequestParam String reversalReason,
            @RequestParam String reversedBy) {
        var result = reverseJournalEntryUseCase.execute(id, reversalDate, reversalReason, reversedBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/{id}/lines")
    @Operation(summary = "Get journal entry lines")
    public ResponseEntity<List<JournalEntryLineResponse>> getLines(@PathVariable UUID id) {
        List<JournalEntryLine> lines = journalEntryLineRepository.findByJournalEntry(
                journalEntryRepository.findById(id).orElseThrow()
        );
        return ResponseEntity.ok(lines.stream().map(this::toLineResponse).toList());
    }

    private JournalEntryResponse toResponse(JournalEntry je) {
        return new JournalEntryResponse(
                je.getId(),
                je.getReference(),
                je.getEntryDate(),
                je.getFiscalPeriod() != null ? je.getFiscalPeriod().getId() : null,
                je.getDescription(),
                je.getStatus(),
                je.getTotalDebit(),
                je.getTotalCredit(),
                je.isReversed(),
                je.getPostedAt(),
                je.getPostedBy(),
                je.getCreatedAt()
        );
    }

    private JournalEntryLineResponse toLineResponse(JournalEntryLine line) {
        return new JournalEntryLineResponse(
                line.getId(),
                line.getLineNumber(),
                line.getAccount() != null ? line.getAccount().getId() : null,
                line.getAccount() != null ? line.getAccount().getCode() : null,
                line.getCostCenter() != null ? line.getCostCenter().getId() : null,
                line.getDescription(),
                line.getDebitAmount(),
                line.getCreditAmount()
        );
    }

    // Request/Response Records
    public record CreateJournalEntryRequest(
            @NotNull String reference,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
            String description,
            @NotNull List<CreateJournalEntryLineRequest> lines
    ) {}

    public record CreateJournalEntryLineRequest(
            @NotNull Integer lineNumber,
            @NotNull UUID accountId,
            UUID costCenterId,
            String description,
            @NotNull java.math.BigDecimal debitAmount,
            @NotNull java.math.BigDecimal creditAmount
    ) {}

    public record JournalEntryResponse(
            UUID id,
            String reference,
            LocalDate entryDate,
            UUID fiscalPeriodId,
            String description,
            JournalEntryStatus status,
            java.math.BigDecimal totalDebit,
            java.math.BigDecimal totalCredit,
            boolean reversed,
            LocalDateTime postedAt,
            String postedBy,
            LocalDateTime createdAt
    ) {}

    public record JournalEntryLineResponse(
            UUID id,
            Integer lineNumber,
            UUID accountId,
            String accountCode,
            UUID costCenterId,
            String description,
            java.math.BigDecimal debitAmount,
            java.math.BigDecimal creditAmount
    ) {}
}