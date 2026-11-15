package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.application.usecase.ProcessPurchaseInvoiceUseCase;
import com.erp.brazil.inventory.domain.model.PurchaseInvoice;
import com.erp.brazil.inventory.domain.model.PurchaseInvoiceItem;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceItemRepository;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseInvoiceDTO;
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
@RequestMapping("/api/v1/purchase-invoices")
@RequiredArgsConstructor
@Tag(name = "Purchase Invoices", description = "Purchase invoice management APIs")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceRepository piRepository;
    private final PurchaseInvoiceItemRepository piiRepository;
    private final ProcessPurchaseInvoiceUseCase processPurchaseInvoiceUseCase;

    @PostMapping
    @Operation(summary = "Create a new purchase invoice")
    public ResponseEntity<PurchaseInvoice> createPurchaseInvoice(@RequestBody PurchaseInvoice pi) {
        PurchaseInvoice saved = piRepository.save(pi);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase invoice by ID")
    public ResponseEntity<PurchaseInvoice> getPurchaseInvoice(@PathVariable UUID id) {
        return piRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List purchase invoices")
    public ResponseEntity<Page<PurchaseInvoice>> listPurchaseInvoices(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID purchaseOrderId,
            @RequestParam(required = false) UUID purchaseReceiptId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {

        Page<PurchaseInvoice> invoices;
        if (status != null) {
            invoices = piRepository.findByCompanyIdAndStatus(companyId, PurchaseInvoice.PIStatus.valueOf(status), pageable);
        } else if (supplierId != null) {
            invoices = piRepository.findByCompanyIdAndSupplierId(companyId, supplierId, pageable);
        } else if (purchaseOrderId != null) {
            invoices = piRepository.findByPurchaseOrderId(purchaseOrderId, pageable);
        } else if (purchaseReceiptId != null) {
            invoices = piRepository.findByPurchaseReceiptId(purchaseReceiptId, pageable);
        } else if (fromDate != null && toDate != null) {
            invoices = piRepository.findByCompanyIdAndInvoiceDateBetween(companyId, fromDate, toDate, pageable);
        } else {
            invoices = Page.empty(pageable);
        }
        return ResponseEntity.ok(invoices);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process invoice (3-way match)")
    public ResponseEntity<PurchaseInvoice> processInvoice(@PathVariable UUID id) {
        PurchaseInvoice invoice = piRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
        PurchaseInvoice processed = processPurchaseInvoiceUseCase.processInvoice(invoice.getCompanyId(), id);
        return ResponseEntity.ok(processed);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit purchase invoice")
    public ResponseEntity<PurchaseInvoice> submitPurchaseInvoice(@PathVariable UUID id) {
        return piRepository.findById(id)
                .map(pi -> {
                    pi.setStatus(PurchaseInvoice.PIStatus.SUBMITTED);
                    return ResponseEntity.ok(piRepository.save(pi));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve purchase invoice")
    public ResponseEntity<PurchaseInvoice> approvePurchaseInvoice(@PathVariable UUID id) {
        return piRepository.findById(id)
                .map(pi -> {
                    pi.setStatus(PurchaseInvoice.PIStatus.APPROVED);
                    return ResponseEntity.ok(piRepository.save(pi));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block invoice for payment")
    public ResponseEntity<PurchaseInvoice> blockInvoice(@PathVariable UUID id, @RequestParam String reason) {
        return piRepository.findById(id)
                .map(pi -> {
                    pi.setStatus(PurchaseInvoice.PIStatus.BLOCKED);
                    pi.setIsBlockedForPayment(true);
                    pi.setBlockReason(reason);
                    return ResponseEntity.ok(piRepository.save(pi));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Unblock invoice for payment")
    public ResponseEntity<PurchaseInvoice> unblockInvoice(@PathVariable UUID id) {
        return piRepository.findById(id)
                .map(pi -> {
                    pi.setStatus(PurchaseInvoice.PIStatus.VALIDATED);
                    pi.setIsBlockedForPayment(false);
                    pi.setBlockReason(null);
                    return ResponseEntity.ok(piRepository.save(pi));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get purchase invoice items")
    public ResponseEntity<List<PurchaseInvoiceItem>> getItems(@PathVariable UUID id) {
        List<PurchaseInvoiceItem> items = piiRepository.findByPurchaseInvoiceIdOrderByIdxAsc(id);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}/match-status")
    @Operation(summary = "Get 3-way match status")
    public ResponseEntity<List<ProcessPurchaseInvoiceUseCase.ThreeWayMatchResult.ItemMatchResult>> getMatchStatus(@PathVariable UUID id) {
        // This would return the match results
        return ResponseEntity.ok(List.of());
    }
}