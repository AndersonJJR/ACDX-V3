package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.application.usecase.ProcessBankStatementUseCase;
import com.erp.brazil.finance.domain.model.BankAccount;
import com.erp.brazil.finance.domain.model.BankStatement;
import com.erp.brazil.finance.domain.model.BankStatementLine;
import com.erp.brazil.finance.domain.model.BankStatementStatus;
import com.erp.brazil.finance.domain.model.Reconciliation;
import com.erp.brazil.finance.domain.model.ReconciliationStatus;
import com.erp.brazil.finance.domain.repository.BankAccountRepository;
import com.erp.brazil.finance.domain.repository.BankStatementLineRepository;
import com.erp.brazil.finance.domain.repository.BankStatementRepository;
import com.erp.brazil.finance.domain.repository.ReconciliationRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bank-reconciliation")
@Tag(name = "Bank Reconciliation", description = "Bank Statement and Reconciliation APIs")
public class BankReconciliationController {

    private final BankAccountRepository bankAccountRepository;
    private final BankStatementRepository bankStatementRepository;
    private final BankStatementLineRepository bankStatementLineRepository;
    private final ReconciliationRepository reconciliationRepository;
    private final ProcessBankStatementUseCase processBankStatementUseCase;

    public BankReconciliationController(BankAccountRepository bankAccountRepository,
                                        BankStatementRepository bankStatementRepository,
                                        BankStatementLineRepository bankStatementLineRepository,
                                        ReconciliationRepository reconciliationRepository,
                                        ProcessBankStatementUseCase processBankStatementUseCase) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankStatementRepository = bankStatementRepository;
        this.bankStatementLineRepository = bankStatementLineRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.processBankStatementUseCase = processBankStatementUseCase;
    }

    // Bank Statements
    @PostMapping("/statements/import")
    @Operation(summary = "Import bank statement from file")
    public ResponseEntity<ProcessBankStatementUseCase.Result> importStatement(
            @RequestParam UUID bankAccountId,
            @RequestParam MultipartFile file,
            @RequestParam String importedBy) throws IOException {
        String content = new String(file.getBytes());
        var result = processBankStatementUseCase.importStatement(bankAccountId, content, file.getOriginalFilename(), importedBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/statements/{id}")
    @Operation(summary = "Get bank statement by ID")
    public ResponseEntity<BankStatementResponse> getStatement(@PathVariable UUID id) {
        return bankStatementRepository.findById(id)
                .map(this::toStatementResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/statements")
    @Operation(summary = "List bank statements")
    public ResponseEntity<Page<BankStatementResponse>> listStatements(
            @Parameter(description = "Bank account ID") @RequestParam(required = false) UUID bankAccountId,
            @Parameter(description = "Status filter") @RequestParam(required = false) BankStatementStatus status,
            @Parameter(description = "Date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<BankStatement> statements;

        if (bankAccountId != null) {
            BankAccount bankAccount = bankAccountRepository.findById(bankAccountId).orElseThrow();
            statements = bankStatementRepository.findByBankAccount(bankAccount, pageable);
        } else if (status != null) {
            statements = bankStatementRepository.findByStatus(status, pageable);
        } else {
            statements = bankStatementRepository.findAll(pageable);
        }

        return ResponseEntity.ok(statements.map(this::toStatementResponse));
    }

    @GetMapping("/statements/{id}/lines")
    @Operation(summary = "Get bank statement lines")
    public ResponseEntity<Page<BankStatementLineResponse>> getStatementLines(
            @PathVariable UUID id,
            @PageableDefault(size = 50) Pageable pageable) {
        BankStatement statement = bankStatementRepository.findById(id).orElseThrow();
        Page<BankStatementLine> lines = bankStatementLineRepository.findByStatement(statement, pageable);
        return ResponseEntity.ok(lines.map(this::toLineResponse));
    }

    @PostMapping("/statements/{id}/reconcile")
    @Operation(summary = "Auto-reconcile bank statement")
    public ResponseEntity<ProcessBankStatementUseCase.Result> reconcileStatement(
            @PathVariable UUID id,
            @RequestParam String reconciledBy) {
        var result = processBankStatementUseCase.autoReconcile(id, reconciledBy);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    // Reconciliations
    @PostMapping("/reconciliations")
    @Operation(summary = "Create new reconciliation")
    public ResponseEntity<ReconciliationResponse> createReconciliation(@Valid @RequestBody CreateReconciliationRequest request) {
        BankAccount bankAccount = bankAccountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found: " + request.bankAccountId()));

        Reconciliation reconciliation = new Reconciliation(
                bankAccount,
                request.reconciliationDate(),
                request.bookBalance(),
                request.bankBalance()
        );
        reconciliation.setReference(request.reference());
        reconciliation.setNotes(request.notes());

        Reconciliation saved = reconciliationRepository.save(reconciliation);
        return ResponseEntity.created(URI.create("/api/v1/bank-reconciliation/reconciliations/" + saved.getId()))
                .body(toReconciliationResponse(saved));
    }

    @GetMapping("/reconciliations/{id}")
    @Operation(summary = "Get reconciliation by ID")
    public ResponseEntity<ReconciliationResponse> getReconciliation(@PathVariable UUID id) {
        return reconciliationRepository.findById(id)
                .map(this::toReconciliationResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reconciliations")
    @Operation(summary = "List reconciliations")
    public ResponseEntity<Page<ReconciliationResponse>> listReconciliations(
            @Parameter(description = "Bank account ID") @RequestParam(required = false) UUID bankAccountId,
            @Parameter(description = "Status filter") @RequestParam(required = false) ReconciliationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Reconciliation> reconciliations;

        if (bankAccountId != null) {
            BankAccount bankAccount = bankAccountRepository.findById(bankAccountId).orElseThrow();
            reconciliations = reconciliationRepository.findByBankAccount(bankAccount, pageable);
        } else if (status != null) {
            reconciliations = reconciliationRepository.findByStatus(status, pageable);
        } else {
            reconciliations = reconciliationRepository.findAll(pageable);
        }

        return ResponseEntity.ok(reconciliations.map(this::toReconciliationResponse));
    }

    // Response Records
    public record BankStatementResponse(
            UUID id,
            UUID bankAccountId,
            String bankAccountCode,
            LocalDate periodStart,
            LocalDate periodEnd,
            java.math.BigDecimal initialBalance,
            java.math.BigDecimal finalBalance,
            BankStatementStatus status,
            java.time.LocalDateTime importedAt,
            String importedBy
    ) {}

    public record BankStatementLineResponse(
            UUID id,
            UUID statementId,
            LocalDate transactionDate,
            LocalDate valueDate,
            String description,
            java.math.BigDecimal amount,
            com.erp.brazil.finance.domain.model.TransactionType type,
            String cnabCode,
            String documentNumber,
            String payerPayee
    ) {}

    public record ReconciliationResponse(
            UUID id,
            UUID bankAccountId,
            String bankAccountCode,
            LocalDate reconciliationDate,
            String reference,
            java.math.BigDecimal bookBalance,
            java.math.BigDecimal bankBalance,
            java.math.BigDecimal difference,
            ReconciliationStatus status,
            java.time.LocalDateTime completedAt,
            String completedBy
    ) {}

    public record CreateReconciliationRequest(
            @NotNull UUID bankAccountId,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reconciliationDate,
            String reference,
            @NotNull java.math.BigDecimal bookBalance,
            @NotNull java.math.BigDecimal bankBalance,
            String notes
    ) {}

    private BankStatementResponse toStatementResponse(BankStatement s) {
        return new BankStatementResponse(
                s.getId(),
                s.getBankAccount().getId(),
                s.getBankAccount().getCode(),
                s.getPeriodStart(),
                s.getPeriodEnd(),
                s.getInitialBalance(),
                s.getFinalBalance(),
                s.getStatus(),
                s.getImportedAt(),
                s.getImportedBy()
        );
    }

    private BankStatementLineResponse toLineResponse(BankStatementLine line) {
        return new BankStatementLineResponse(
                line.getId(),
                line.getStatement().getId(),
                line.getTransactionDate(),
                line.getValueDate(),
                line.getDescription(),
                line.getAmount(),
                line.getType(),
                line.getCnabCode(),
                line.getDocumentNumber(),
                line.getPayerPayee()
        );
    }

    private ReconciliationResponse toReconciliationResponse(Reconciliation r) {
        return new ReconciliationResponse(
                r.getId(),
                r.getBankAccount().getId(),
                r.getBankAccount().getCode(),
                r.getReconciliationDate(),
                r.getReference(),
                r.getBookBalance(),
                r.getBankBalance(),
                r.getDifference(),
                r.getStatus(),
                r.getCompletedAt(),
                r.getCompletedBy()
        );
    }
}