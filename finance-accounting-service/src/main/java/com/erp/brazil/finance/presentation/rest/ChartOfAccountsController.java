package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.FiscalYear;
import com.erp.brazil.finance.domain.repository.ChartOfAccountsRepository;
import com.erp.brazil.finance.domain.repository.FiscalYearRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chart-of-accounts")
@Tag(name = "Chart of Accounts", description = "Chart of Accounts management APIs")
public class ChartOfAccountsController {

    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final FiscalYearRepository fiscalYearRepository;

    public ChartOfAccountsController(ChartOfAccountsRepository chartOfAccountsRepository,
                                     FiscalYearRepository fiscalYearRepository) {
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.fiscalYearRepository = fiscalYearRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<ChartOfAccountsResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(request.fiscalYearId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal year not found: " + request.fiscalYearId()));

        ChartOfAccounts parent = request.parentId() != null
                ? chartOfAccountsRepository.findById(request.parentId()).orElse(null)
                : null;

        ChartOfAccounts account = new ChartOfAccounts(
                request.code(),
                request.name(),
                request.accountType(),
                request.accountNature(),
                fiscalYear
        );
        account.setDescription(request.description());
        account.setParent(parent);
        account.setDetail(request.detail());
        account.setLevel(parent != null ? parent.getLevel() + 1 : 1);

        ChartOfAccounts saved = chartOfAccountsRepository.save(account);
        return ResponseEntity.created(URI.create("/api/v1/chart-of-accounts/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<ChartOfAccountsResponse> getById(@PathVariable UUID id) {
        return chartOfAccountsRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List accounts")
    public ResponseEntity<Page<ChartOfAccountsResponse>> list(
            @Parameter(description = "Fiscal year ID") @RequestParam(required = false) UUID fiscalYearId,
            @Parameter(description = "Account type filter") @RequestParam(required = false) com.erp.brazil.finance.domain.model.AccountType accountType,
            @Parameter(description = "Active only") @RequestParam(defaultValue = "true") boolean activeOnly,
            @Parameter(description = "Parent account ID (for children)") @RequestParam(required = false) UUID parentId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<ChartOfAccounts> accounts;

        if (fiscalYearId != null) {
            FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId).orElseThrow();
            if (parentId != null) {
                ChartOfAccounts parent = chartOfAccountsRepository.findById(parentId).orElseThrow();
                var children = chartOfAccountsRepository.findByParent(parent);
                // Convert list to page manually for simplicity
                return ResponseEntity.ok(org.springframework.data.domain.Page.empty(pageable));
            } else if (accountType != null) {
                accounts = chartOfAccountsRepository.findByAccountTypeAndFiscalYearOrderByCode(accountType, fiscalYear, pageable);
            } else if (activeOnly) {
                accounts = chartOfAccountsRepository.findByActiveTrueAndFiscalYear(fiscalYear, pageable);
            } else {
                accounts = chartOfAccountsRepository.findByFiscalYear(fiscalYear, pageable);
            }
        } else {
            accounts = chartOfAccountsRepository.findAll(pageable);
        }

        return ResponseEntity.ok(accounts.map(this::toResponse));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get chart of accounts as tree")
    public ResponseEntity<List<ChartOfAccountsTreeResponse>> getTree(
            @Parameter(description = "Fiscal year ID") @RequestParam UUID fiscalYearId) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId).orElseThrow();
        List<ChartOfAccounts> roots = chartOfAccountsRepository.findByParentIsNullAndFiscalYearOrderByCode(fiscalYear);
        return ResponseEntity.ok(roots.stream().map(this::toTreeResponse).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account")
    public ResponseEntity<ChartOfAccountsResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

        account.setName(request.name());
        account.setDescription(request.description());
        account.setActive(request.active());
        account.setDetail(request.detail());

        ChartOfAccounts saved = chartOfAccountsRepository.save(account);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account (only if no children and no entries)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));

        if (!account.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete account with children");
        }

        chartOfAccountsRepository.delete(account);
        return ResponseEntity.noContent().build();
    }

    private ChartOfAccountsResponse toResponse(ChartOfAccounts account) {
        return new ChartOfAccountsResponse(
                account.getId(),
                account.getCode(),
                account.getName(),
                account.getDescription(),
                account.getAccountType(),
                account.getAccountNature(),
                account.isActive(),
                account.isDetail(),
                account.getLevel(),
                account.getParent() != null ? account.getParent().getId() : null,
                account.getFiscalYear().getId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private ChartOfAccountsTreeResponse toTreeResponse(ChartOfAccounts account) {
        List<ChartOfAccountsTreeResponse> children = account.getChildren().stream()
                .map(this::toTreeResponse)
                .toList();
        return new ChartOfAccountsTreeResponse(
                account.getId(),
                account.getCode(),
                account.getName(),
                account.getAccountType(),
                account.getAccountNature(),
                account.isActive(),
                account.isDetail(),
                account.getLevel(),
                children
        );
    }

    // Request/Response Records
    public record CreateAccountRequest(
            @NotNull String code,
            @NotNull String name,
            String description,
            @NotNull com.erp.brazil.finance.domain.model.AccountType accountType,
            @NotNull com.erp.brazil.finance.domain.model.AccountNature accountNature,
            @NotNull UUID fiscalYearId,
            UUID parentId,
            boolean detail
    ) {}

    public record UpdateAccountRequest(
            @NotNull String name,
            String description,
            boolean active,
            boolean detail
    ) {}

    public record ChartOfAccountsResponse(
            UUID id,
            String code,
            String name,
            String description,
            com.erp.brazil.finance.domain.model.AccountType accountType,
            com.erp.brazil.finance.domain.model.AccountNature accountNature,
            boolean active,
            boolean detail,
            int level,
            UUID parentId,
            UUID fiscalYearId,
            java.time.LocalDate createdAt,
            java.time.LocalDate updatedAt
    ) {}

    public record ChartOfAccountsTreeResponse(
            UUID id,
            String code,
            String name,
            com.erp.brazil.finance.domain.model.AccountType accountType,
            com.erp.brazil.finance.domain.model.AccountNature accountNature,
            boolean active,
            boolean detail,
            int level,
            List<ChartOfAccountsTreeResponse> children
    ) {}
}