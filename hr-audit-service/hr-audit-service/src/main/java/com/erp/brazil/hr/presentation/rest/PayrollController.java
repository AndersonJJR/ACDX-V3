package com.erp.brazil.hr.presentation.rest;

import com.erp.brazil.hr.application.dto.PayrollDTO;
import com.erp.brazil.hr.application.dto.PayrollItemDTO;
import com.erp.brazil.hr.application.usecase.ProcessPayrollUseCase;
import com.erp.brazil.hr.application.usecase.ProcessWebhookUseCase;
import com.erp.brazil.hr.domain.model.Payroll;
import com.erp.brazil.hr.domain.model.PayrollItem;
import com.erp.brazil.hr.domain.repository.PayrollRepository;
import com.erp.brazil.hr.domain.repository.PayrollItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll")
@Tag(name = "Payroll", description = "Payroll processing API")
public class PayrollController {

    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final ProcessPayrollUseCase processPayrollUseCase;
    private final ProcessWebhookUseCase webhookUseCase;

    public PayrollController(PayrollRepository payrollRepository,
                              PayrollItemRepository payrollItemRepository,
                              ProcessPayrollUseCase processPayrollUseCase,
                              ProcessWebhookUseCase webhookUseCase) {
        this.payrollRepository = payrollRepository;
        this.payrollItemRepository = payrollItemRepository;
        this.processPayrollUseCase = processPayrollUseCase;
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new payroll period")
    public ResponseEntity<PayrollDTO> create(@Valid @RequestBody PayrollDTO dto) {
        Payroll payroll = new Payroll(
            null, UUID.randomUUID(), dto.companyId(), dto.referenceMonth(), dto.referenceYear(),
            Payroll.PayrollStatus.DRAFT, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            null, null, null, null, null, null, null, null, null, dto.observations()
        );

        Payroll saved = payrollRepository.save(payroll);
        return ResponseEntity.status(HttpStatus.CREATED).body(PayrollDTO.from(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payroll by ID")
    public ResponseEntity<PayrollDTO> getById(@PathVariable UUID id) {
        return payrollRepository.findByUuid(id)
            .map(PayrollDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List payrolls with filters")
    public ResponseEntity<Page<PayrollDTO>> list(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Status") @RequestParam(required = false) Payroll.PayrollStatus status,
            @Parameter(description = "Year") @RequestParam(required = false) Integer year,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PayrollDTO> page;
        if (status != null) {
            page = payrollRepository.findByCompanyIdAndStatus(companyId, status, pageable)
                .map(PayrollDTO::from);
        } else if (year != null) {
            page = payrollRepository.findByCompanyIdAndReferenceYear(companyId, year, pageable)
                .map(PayrollDTO::from);
        } else {
            page = payrollRepository.findByCompanyId(companyId, pageable)
                .map(PayrollDTO::from);
        }
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process payroll calculation (async)")
    public ResponseEntity<PayrollDTO> process(@PathVariable UUID id) {
        Payroll payroll = payrollRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found: " + id));

        // Start async processing
        processPayrollUseCase.processAsync(payroll.id())
            .thenAccept(result -> webhookUseCase.triggerEvent("PAYROLL_PROCESSED", Map.of("payroll", result), result.companyId()));

        return ResponseEntity.accepted().body(PayrollDTO.from(payroll));
    }

    @PostMapping("/{id}/process-sync")
    @Operation(summary = "Process payroll calculation (synchronous)")
    public ResponseEntity<PayrollDTO> processSync(@PathVariable UUID id) {
        Payroll payroll = payrollRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found: " + id));

        Payroll processed = processPayrollUseCase.process(payroll.id());
        webhookUseCase.triggerEvent("PAYROLL_PROCESSED", Map.of("payroll", processed), processed.companyId());
        return ResponseEntity.ok(PayrollDTO.from(processed));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get payroll processing status")
    public ResponseEntity<PayrollDTO> getStatus(@PathVariable UUID id) {
        return payrollRepository.findByUuid(id)
            .map(PayrollDTO::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get payroll items")
    public ResponseEntity<List<PayrollItemDTO>> getItems(@PathVariable UUID id) {
        Payroll payroll = payrollRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found: " + id));

        List<PayrollItemDTO> items = payrollItemRepository.findByPayrollIdOrderByDisplayOrder(payroll.id())
            .stream().map(PayrollItemDTO::from).toList();
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve calculated payroll")
    public ResponseEntity<PayrollDTO> approve(@PathVariable UUID id, @RequestParam Long approvedBy) {
        Payroll payroll = payrollRepository.findByUuid(id)
            .orElseThrow(() -> new IllegalArgumentException("Payroll not found: " + id));

        if (payroll.status() != Payroll.PayrollStatus.CALCULATED) {
            throw new IllegalStateException("Payroll must be in CALCULATED status to approve");
        }

        Payroll approved = new Payroll(
            payroll.id(), payroll.uuid(), payroll.companyId(), payroll.referenceMonth(), payroll.referenceYear(),
            Payroll.PayrollStatus.APPROVED, payroll.totalEmployees(), payroll.totalGrossAmount(),
            payroll.totalNetAmount(), payroll.totalInssAmount(), payroll.totalIrrfAmount(),
            payroll.totalFgtsAmount(), payroll.totalBenefitsAmount(), payroll.totalDeductionsAmount(),
            payroll.calculatedAt(), payroll.calculatedBy(), java.time.LocalDateTime.now(), approvedBy,
            payroll.processedAt(), payroll.processedBy(), payroll.paidAt(), payroll.paidBy(),
            payroll.errorMessage(), payroll.observations()
        );

        Payroll saved = payrollRepository.save(approved);
        webhookUseCase.triggerEvent("PAYROLL_APPROVED", Map.of("payroll", saved), saved.companyId());
        return ResponseEntity.ok(PayrollDTO.from(saved));
    }
}