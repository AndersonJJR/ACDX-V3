package com.erp.brazil.finance.presentation.rest;

import com.erp.brazil.finance.domain.model.InvoiceStatus;
import com.erp.brazil.finance.domain.model.PayableInvoice;
import com.erp.brazil.finance.domain.model.Vendor;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.Currency;
import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.repository.PayableInvoiceRepository;
import com.erp.brazil.finance.domain.repository.VendorRepository;
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
@RequestMapping("/api/v1/payables")
@Tag(name = "Payables", description = "Accounts Payable management APIs")
public class PayableController {

    private final PayableInvoiceRepository payableInvoiceRepository;
    private final VendorRepository vendorRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final CurrencyRepository currencyRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;

    public PayableController(PayableInvoiceRepository payableInvoiceRepository,
                             VendorRepository vendorRepository,
                             FiscalPeriodRepository fiscalPeriodRepository,
                             CurrencyRepository currencyRepository,
                             ChartOfAccountsRepository chartOfAccountsRepository) {
        this.payableInvoiceRepository = payableInvoiceRepository;
        this.vendorRepository = vendorRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.currencyRepository = currencyRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new payable invoice")
    public ResponseEntity<PayableInvoiceResponse> create(@Valid @RequestBody CreatePayableInvoiceRequest request) {
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + request.vendorId()));

        FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(request.fiscalPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Fiscal period not found: " + request.fiscalPeriodId()));

        Currency currency = currencyRepository.findById(request.currencyId())
                .orElseThrow(() -> new IllegalArgumentException("Currency not found: " + request.currencyId()));

        ChartOfAccounts payableAccount = chartOfAccountsRepository.findById(request.payableAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Payable account not found: " + request.payableAccountId()));

        PayableInvoice invoice = new PayableInvoice(
                request.companyId(),
                vendor,
                request.invoiceNumber(),
                request.invoiceSeries(),
                request.invoiceDate(),
                request.dueDate(),
                fiscalPeriod,
                request.totalAmount(),
                currency,
                payableAccount
        );
        invoice.setTaxAmount(request.taxAmount());
        invoice.setDiscountAmount(request.discountAmount());
        invoice.setDescription(request.description());
        invoice.setPurchaseOrderNumber(request.purchaseOrderNumber());
        invoice.setGoodsReceiptNumber(request.goodsReceiptNumber());

        PayableInvoice saved = payableInvoiceRepository.save(invoice);
        return ResponseEntity.created(URI.create("/api/v1/payables/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payable invoice by ID")
    public ResponseEntity<PayableInvoiceResponse> getById(@PathVariable UUID id) {
        return payableInvoiceRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List payable invoices")
    public ResponseEntity<Page<PayableInvoiceResponse>> list(
            @Parameter(description = "Vendor ID") @RequestParam(required = false) UUID vendorId,
            @Parameter(description = "Fiscal period ID") @RequestParam(required = false) UUID fiscalPeriodId,
            @Parameter(description = "Status filter") @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Due date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @Parameter(description = "Due date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @Parameter(description = "Show only open invoices") @RequestParam(defaultValue = "false") boolean openOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PayableInvoice> invoices;

        if (vendorId != null) {
            Vendor vendor = vendorRepository.findById(vendorId).orElseThrow();
            invoices = payableInvoiceRepository.findByVendor(vendor, pageable);
        } else if (fiscalPeriodId != null) {
            FiscalPeriod fiscalPeriod = fiscalPeriodRepository.findById(fiscalPeriodId).orElseThrow();
            invoices = payableInvoiceRepository.findByFiscalPeriod(fiscalPeriod, pageable);
        } else if (status != null) {
            invoices = payableInvoiceRepository.findByStatus(status, pageable);
        } else if (openOnly) {
            invoices = payableInvoiceRepository.findByStatusIn(List.of(InvoiceStatus.APPROVED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.OVERDUE), pageable);
        } else {
            invoices = payableInvoiceRepository.findAll(pageable);
        }

        return ResponseEntity.ok(invoices.map(this::toResponse));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue payable invoices")
    public ResponseEntity<List<PayableInvoiceResponse>> getOverdue(
            @Parameter(description = "As of date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        List<PayableInvoice> invoices = payableInvoiceRepository.findOverdueInvoices(asOfDate);
        return ResponseEntity.ok(invoices.stream().map(this::toResponse).toList());
    }

    @GetMapping("/vendor/{vendorId}/open")
    @Operation(summary = "Get open invoices for a vendor")
    public ResponseEntity<List<PayableInvoiceResponse>> getOpenByVendor(@PathVariable UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId).orElseThrow();
        List<PayableInvoice> invoices = payableInvoiceRepository.findOpenInvoicesByVendor(vendor);
        return ResponseEntity.ok(invoices.stream().map(this::toResponse).toList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payable invoice")
    public ResponseEntity<PayableInvoiceResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdatePayableInvoiceRequest request) {
        PayableInvoice invoice = payableInvoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update paid or cancelled invoice");
        }

        invoice.setDescription(request.description());
        invoice.setDueDate(request.dueDate());

        PayableInvoice saved = payableInvoiceRepository.save(invoice);
        return ResponseEntity.ok(toResponse(saved));
    }

    private PayableInvoiceResponse toResponse(PayableInvoice invoice) {
        return new PayableInvoiceResponse(
                invoice.getId(),
                invoice.getVendor().getId(),
                invoice.getVendor().getCode(),
                invoice.getVendor().getName(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceSeries(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getFiscalPeriod().getId(),
                invoice.getTotalAmount(),
                invoice.getTaxAmount(),
                invoice.getDiscountAmount(),
                invoice.getPaidAmount(),
                invoice.getOutstandingAmount(),
                invoice.getCurrency().getCode(),
                invoice.getExchangeRate(),
                invoice.getPayableAccount().getCode(),
                invoice.getStatus(),
                invoice.getApprovalDate(),
                invoice.getApprovedBy(),
                invoice.getDescription(),
                invoice.getPurchaseOrderNumber(),
                invoice.getGoodsReceiptNumber(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    // Request/Response Records
    public record CreatePayableInvoiceRequest(
            @NotNull UUID companyId,
            @NotNull UUID vendorId,
            @NotNull String invoiceNumber,
            String invoiceSeries,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate invoiceDate,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @NotNull UUID fiscalPeriodId,
            @NotNull java.math.BigDecimal totalAmount,
            java.math.BigDecimal taxAmount,
            java.math.BigDecimal discountAmount,
            @NotNull UUID currencyId,
            @NotNull UUID payableAccountId,
            String description,
            String purchaseOrderNumber,
            String goodsReceiptNumber
    ) {}

    public record UpdatePayableInvoiceRequest(
            String description,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
    ) {}

    public record PayableInvoiceResponse(
            UUID id,
            UUID vendorId,
            String vendorCode,
            String vendorName,
            String invoiceNumber,
            String invoiceSeries,
            LocalDate invoiceDate,
            LocalDate dueDate,
            UUID fiscalPeriodId,
            java.math.BigDecimal totalAmount,
            java.math.BigDecimal taxAmount,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal paidAmount,
            java.math.BigDecimal outstandingAmount,
            String currencyCode,
            java.math.BigDecimal exchangeRate,
            String payableAccountCode,
            InvoiceStatus status,
            LocalDate approvalDate,
            String approvedBy,
            String description,
            String purchaseOrderNumber,
            String goodsReceiptNumber,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {}
}