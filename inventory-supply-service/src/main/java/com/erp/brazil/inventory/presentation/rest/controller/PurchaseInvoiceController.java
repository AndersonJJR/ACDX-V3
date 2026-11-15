package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.application.usecase.ProcessPurchaseInvoiceUseCase;
import com.erp.brazil.inventory.domain.model.PurchaseInvoice;
import com.erp.brazil.inventory.domain.model.PurchaseInvoiceItem;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceItemRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptRepository;
import com.erp.brazil.inventory.domain.repository.SupplierRepository;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseInvoiceItemResponse;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseInvoiceResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/purchase-invoices")
@RequiredArgsConstructor
@Tag(name = "Purchase Invoices", description = "Purchase invoice management with 3-way matching")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceItemRepository purchaseInvoiceItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final SupplierRepository supplierRepository;
    private final ProcessPurchaseInvoiceUseCase processPurchaseInvoiceUseCase;

    @GetMapping
    @Operation(summary = "List purchase invoices")
    public ResponseEntity<Page<PurchaseInvoiceResponse>> listPurchaseInvoices(
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<PurchaseInvoice> invoices;
        if (status != null) {
            invoices = purchaseInvoiceRepository.findByCompanyIdAndStatus(
                    companyId, PurchaseInvoice.PIStatus.valueOf(status), pageable);
        } else {
            invoices = purchaseInvoiceRepository.findAll(pageable); // Would need company filter
        }

        return ResponseEntity.ok(invoices.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase invoice by ID")
    public ResponseEntity<PurchaseInvoiceResponse> getPurchaseInvoice(@Parameter(description = "Invoice ID") @PathVariable UUID id) {
        return purchaseInvoiceRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new purchase invoice")
    public ResponseEntity<PurchaseInvoiceResponse> createPurchaseInvoice(@RequestBody PurchaseInvoiceRequest request) {
        var supplierOpt = supplierRepository.findById(request.supplierId());
        if (supplierOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .companyId(request.companyId())
                .invoiceNo(request.invoiceNo())
                .supplier(supplierOpt.get())
                .purchaseOrderId(request.purchaseOrderId() != null ?
                        purchaseOrderRepository.findById(request.purchaseOrderId()).orElse(null) : null)
                .purchaseReceiptId(request.purchaseReceiptId() != null ?
                        purchaseReceiptRepository.findById(request.purchaseReceiptId()).orElse(null) : null)
                .invoiceDate(request.invoiceDate())
                .postingDate(request.postingDate())
                .dueDate(request.dueDate())
                .status(PurchaseInvoice.PIStatus.DRAFT)
                .currency(request.currency() != null ? request.currency() : "BRL")
                .exchangeRate(request.exchangeRate() != null ? request.exchangeRate() : BigDecimal.ONE)
                .paymentTerms(request.paymentTerms())
                .nfeAccessKey(request.nfeAccessKey())
                .nfeNumber(request.nfeNumber())
                .nfeSeries(request.nfeSeries())
                .cfop(request.cfop())
                .naturezaOperacao(request.naturezaOperacao())
                .notes(request.notes())
                .build();

        PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase invoice")
    public ResponseEntity<PurchaseInvoiceResponse> updatePurchaseInvoice(
            @Parameter(description = "Invoice ID") @PathVariable UUID id,
            @RequestBody PurchaseInvoiceRequest request) {

        return purchaseInvoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setInvoiceNo(request.invoiceNo());
                    invoice.setInvoiceDate(request.invoiceDate());
                    invoice.setPostingDate(request.postingDate());
                    invoice.setDueDate(request.dueDate());
                    invoice.setCurrency(request.currency());
                    invoice.setExchangeRate(request.exchangeRate());
                    invoice.setPaymentTerms(request.paymentTerms());
                    invoice.setNfeAccessKey(request.nfeAccessKey());
                    invoice.setNfeNumber(request.nfeNumber());
                    invoice.setNfeSeries(request.nfeSeries());
                    invoice.setCfop(request.cfop());
                    invoice.setNaturezaOperacao(request.naturezaOperacao());
                    invoice.setNotes(request.notes());

                    PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
                    return ResponseEntity.ok(mapToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/match")
    @Operation(summary = "Run 3-way match (PO vs Receipt vs Invoice)")
    public ResponseEntity<ProcessPurchaseInvoiceUseCase.Output> matchInvoice(
            @Parameter(description = "Invoice ID") @PathVariable UUID id) {

        try {
            ProcessPurchaseInvoiceUseCase.Input input = new ProcessPurchaseInvoiceUseCase.Input(id);
            ProcessPurchaseInvoiceUseCase.Output output = processPurchaseInvoiceUseCase.execute(input);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private PurchaseInvoiceResponse mapToResponse(PurchaseInvoice invoice) {
        List<PurchaseInvoiceItemResponse> items = purchaseInvoiceItemRepository.findByPurchaseInvoiceId(invoice.getId())
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        BigDecimal totalQty = items.stream().map(PurchaseInvoiceItemResponse::getQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = items.stream().map(PurchaseInvoiceItemResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = items.stream().map(PurchaseInvoiceItemResponse::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = totalAmount.add(taxAmount);
        BigDecimal outstandingAmount = grandTotal.subtract(invoice.getAdvanceAllocated());

        return PurchaseInvoiceResponse.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .invoiceNo(invoice.getInvoiceNo())
                .supplierId(invoice.getSupplier().getId())
                .supplierName(invoice.getSupplier().getSupplierName())
                .purchaseOrderId(invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getId() : null)
                .purchaseOrderNo(invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getPoNo() : null)
                .purchaseReceiptId(invoice.getPurchaseReceipt() != null ? invoice.getPurchaseReceipt().getId() : null)
                .purchaseReceiptNo(invoice.getPurchaseReceipt() != null ? invoice.getPurchaseReceipt().getReceiptNo() : null)
                .invoiceDate(invoice.getInvoiceDate())
                .postingDate(invoice.getPostingDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus().name())
                .currency(invoice.getCurrency())
                .exchangeRate(invoice.getExchangeRate())
                .totalQty(totalQty)
                .totalAmount(totalAmount)
                .taxAmount(taxAmount)
                .grandTotal(grandTotal)
                .advanceAllocated(invoice.getAdvanceAllocated())
                .outstandingAmount(outstandingAmount)
                .paymentTerms(invoice.getPaymentTerms())
                .nfeAccessKey(invoice.getNfeAccessKey())
                .nfeNumber(invoice.getNfeNumber())
                .nfeSeries(invoice.getNfeSeries())
                .nfeProtocol(invoice.getNfeProtocol())
                .nfeStatus(invoice.getNfeStatus() != null ? invoice.getNfeStatus().name() : null)
                .cfop(invoice.getCfop())
                .naturezaOperacao(invoice.getNaturezaOperacao())
                .notes(invoice.getNotes())
                .isBlockedForPayment(invoice.getIsBlockedForPayment())
                .blockReason(invoice.getBlockReason())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .items(items)
                .build();
    }

    private PurchaseInvoiceItemResponse mapItemToResponse(PurchaseInvoiceItem item) {
        return PurchaseInvoiceItemResponse.builder()
                .id(item.getId())
                .companyId(item.getCompanyId())
                .purchaseInvoiceId(item.getPurchaseInvoice().getId())
                .idx(item.getIdx())
                .purchaseOrderItemId(item.getPurchaseOrderItemId())
                .purchaseReceiptItemId(item.getPurchaseReceiptItemId())
                .itemId(item.getItem().getId())
                .itemCode(item.getItem().getCode())
                .itemName(item.getItem().getName())
                .itemVariantId(item.getItemVariantId())
                .variantCode(item.getItemVariantId() != null ? item.getItemVariant().getVariantCode() : null)
                .qty(item.getQty())
                .uom(item.getUom())
                .conversionFactor(item.getConversionFactor())
                .rate(item.getRate())
                .amount(item.getAmount())
                .discountPercent(item.getDiscountPercent())
                .discountAmount(item.getDiscountAmount())
                .taxTemplate(item.getTaxTemplate())
                .taxAmount(item.getTaxAmount())
                .poQty(item.getPoQty())
                .receivedQty(item.getReceivedQty())
                .billedQty(item.getBilledQty())
                .matchStatus(item.getMatchStatus().name())
                .varianceQty(item.getVarianceQty())
                .varianceAmount(item.getVarianceAmount())
                .description(item.getDescription())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public record PurchaseInvoiceRequest(
            UUID companyId,
            String invoiceNo,
            UUID supplierId,
            UUID purchaseOrderId,
            UUID purchaseReceiptId,
            LocalDate invoiceDate,
            LocalDate postingDate,
            LocalDate dueDate,
            String currency,
            BigDecimal exchangeRate,
            String paymentTerms,
            String nfeAccessKey,
            Long nfeNumber,
            String nfeSeries,
            String cfop,
            String naturezaOperacao,
            String notes
    ) {}
}