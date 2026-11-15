package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.domain.model.InvoiceStatus;
import com.erp.brazil.finance.domain.model.ReceivableInvoice;
import com.erp.brazil.finance.domain.model.Customer;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.Currency;
import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.repository.ReceivableInvoiceRepository;
import com.erp.brazil.finance.domain.repository.CustomerRepository;
import com.erp.brazil.finance.domain.repository.FiscalPeriodRepository;
import com.erp.brazil.finance.domain.repository.CurrencyRepository;
import com.erp.brazil.finance.domain.repository.ChartOfAccountsRepository;
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
@RequestMapping("/api/v1/receivables")
@Tag(name = "Receivables", description = "Accounts Receivable management APIs")
public class ReceivableController {

    private final ReceivableInvoiceRepository receivableInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final CurrencyRepository currencyRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;

    public ReceivableController(ReceivableInvoiceRepository receivableInvoiceRepository,
                                CustomerRepository customerRepository,
                                FiscalPeriodRepository fiscalPeriodRepository,
                                CurrencyRepository currencyRepository,
                                ChartOfAccountsRepository chartOfAccountsRepository) {
        this.receivableInvoiceRepository = receivableInvoiceRepository;
        this.customerRepository = customerRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.currencyRepository = currencyRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new receivable invoice")
    public ResponseEntity<ReceivableInvoiceResponse> create(@Valid @RequestBody CreateReceivableInvoiceRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(request.fiscalPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + request.fiscalPeriodId()));

        Currency currency = currencyRepository.findById(request.currencyId())
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + request.currencyId()));

        ChartOfAccounts receivableAccount = chartOfAccountsRepository.findById(request.receivableAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Receivable account not found: " + request.receivableAccountId()));

        ReceivableInvoice invoice = new ReceivableInvoice(
                request.companyId(),
                customer,
                request.invoiceNumber(),
                request.invoiceSeries(),
                request.invoiceDate(),
                request.dueDate(),
                fiscalPeriod,
                request.totalAmount(),
                currency,
                receivableAccount
        );
        invoice.setTaxAmount(request.taxAmount());
        invoice.setDiscountAmount(request.discountAmount());
        invoice.setDescription(request.description());
        invoice.setSalesOrderNumber(request.salesOrderNumber());
        invoice.setDeliveryNoteNumber(request.deliveryNoteNumber());

        ReceivableInvoice saved = receivableInvoiceRepository.save(invoice);
        return ResponseEntity.created(URI.create("/api/v1/receivables/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get receivable invoice by ID")
    public ResponseEntity<ReceivableInvoiceResponse> getById(@PathVariable UUID id) {
        return receivableInvoiceRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List receivable invoices")
    public ResponseEntity<Page<ReceivableInvoiceResponse>> list(
            @Parameter(description = "Customer ID") @RequestParam(required = false) UUID customerId,
            @Parameter(description = "Fiscal period ID") @RequestParam(required = false) UUID fiscalPeriodId,
            @Parameter(description = "Status filter") @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Due date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @Parameter(description = "Due date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @Parameter(description = "Show only open invoices") @RequestParam(defaultValue = "false") boolean openOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReceivableInvoice> invoices;

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId).orElseThrow();
            invoices = receivableInvoiceRepository.findByCustomer(customer, pageable);
        } else if (fiscalPeriodId != null) {
            FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId).orElseThrow();
            invoices = receivableInvoiceRepository.findByFiscalPeriod(fiscalPeriod, pageable);
        } else if (status != null) {
            invoices = receivableInvoiceRepository.findByStatus(status, pageable);
        } else if (openOnly) {
            invoices = receivableInvoiceRepository.findByStatusIn(List.of(InvoiceStatus.APPROVED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE), pageable);
        } else {
            invoices = receivableInvoiceRepository.findAll(pageable);
        }

        return ResponseEntity.ok(invoices.map(this::toResponse));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue receivable invoices")
    public ResponseEntity<List<ReceivableInvoiceResponse>> getOverdue(
            @Parameter(description = "As of date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        List<ReceivableInvoice> invoices = receivableInvoiceRepository.findOverdueInvoices(asOfDate);
        return ResponseEntity.ok(invoices.stream().map(this::toResponse).toList());
    }

    @GetMapping("/customer/{customerId}/open")
    @Operation(summary = "Get open invoices for a customer")
    public ResponseEntity<List<ReceivableInvoiceResponse>> getOpenByCustomer(@PathVariable UUID customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow();
        List<ReceivableInvoice> invoices = receivableInvoiceRepository.findOpenInvoicesByCustomer(customer);
        return ResponseEntity.ok(invoices.stream().map(this::toResponse).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update receivable invoice")
    public ResponseEntity<ReceivableInvoiceResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateReceivableInvoiceRequest request) {
        ReceivableInvoice invoice = receivableInvoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update paid or cancelled invoice");
        }

        invoice.setDescription(request.description());
        invoice.setDueDate(request.dueDate());

        ReceivableInvoice saved = receivableInvoiceRepository.save(invoice);
        return ResponseEntity.ok(toResponse(saved));
    }

    private ReceivableInvoiceResponse toResponse(ReceivableInvoice invoice) {
        return new ReceivableInvoiceResponse(
                invoice.getId(),
                invoice.getCustomer().getId(),
                invoice.getCustomer().getCode(),
                invoice.getCustomer().getName(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceSeries(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getFiscalPeriod().getId(),
                invoice.getTotalAmount(),
                invoice.getTaxAmount(),
                invoice.getDiscountAmount(),
                invoice.getReceivedAmount(),
                invoice.getOutstandingAmount(),
                invoice.getCurrency().getCode(),
                invoice.getExchangeRate(),
                invoice.getReceivableAccount().getCode(),
                invoice.getStatus(),
                invoice.getApprovalDate(),
                invoice.getApprovedBy(),
                invoice.getDescription(),
                invoice.getSalesOrderNumber(),
                invoice.getDeliveryNoteNumber(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    // Request/Response Records
    public record CreateReceivableInvoiceRequest(
            @NotNull UUID companyId,
            @NotNull UUID customerId,
            @NotNull String invoiceNumber,
            String invoiceSeries,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate invoiceDate,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @NotNull UUID fiscalPeriodId,
            @NotNull java.math.BigDecimal totalAmount,
            java.math.BigDecimal taxAmount,
            java.math.BigDecimal discountAmount,
            @NotNull UUID currencyId,
            @NotNull UUID receivableAccountId,
            String description,
            String salesOrderNumber,
            String deliveryNoteNumber
    ) {}

    public record UpdateReceivableInvoiceRequest(
            String description,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
    ) {}

    public record ReceivableInvoiceResponse(
            UUID id,
            UUID customerId,
            String customerCode,
            String customerName,
            String invoiceNumber,
            String invoiceSeries,
            LocalDate invoiceDate,
            LocalDate dueDate,
            UUID fiscalPeriodId,
            java.math.BigDecimal totalAmount,
            java.math.BigDecimal taxAmount,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal receivedAmount,
            java.math.BigDecimal outstandingAmount,
            String currencyCode,
            java.math.BigDecimal exchangeRate,
            String receivableAccountCode,
            InvoiceStatus status,
            LocalDate approvalDate,
            String approvedBy,
            String description,
            String salesOrderNumber,
            String deliveryNoteNumber,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}
}