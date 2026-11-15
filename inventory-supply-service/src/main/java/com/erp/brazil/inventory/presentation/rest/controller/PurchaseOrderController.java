package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.domain.model.PurchaseOrder;
import com.erp.brazil.inventory.domain.model.PurchaseOrderItem;
import com.erp.brazil.inventory.domain.model.PurchaseReceipt;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderItemRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptRepository;
import com.erp.brazil.inventory.domain.repository.SupplierRepository;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseOrderItemResponse;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseOrderResponse;
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
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Purchase order management")
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final SupplierRepository supplierRepository;

    @GetMapping
    @Operation(summary = "List purchase orders")
    public ResponseEntity<Page<PurchaseOrderResponse>> listPurchaseOrders(
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<PurchaseOrder> orders;
        if (status != null) {
            orders = purchaseOrderRepository.findByCompanyIdAndStatus(
                    companyId, PurchaseOrder.POStatus.valueOf(status), pageable);
        } else {
            orders = purchaseOrderRepository.findAll(pageable); // Would need company filter
        }

        return ResponseEntity.ok(orders.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrder(@Parameter(description = "PO ID") @PathVariable UUID id) {
        return purchaseOrderRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new purchase order")
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@RequestBody PurchaseOrderRequest request) {
        var supplierOpt = supplierRepository.findById(request.supplierId());
        if (supplierOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .companyId(request.companyId())
                .poNo(request.poNo())
                .supplier(supplierOpt.get())
                .poDate(request.poDate())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .status(PurchaseOrder.POStatus.DRAFT)
                .currency(request.currency() != null ? request.currency() : "BRL")
                .exchangeRate(request.exchangeRate() != null ? request.exchangeRate() : BigDecimal.ONE)
                .paymentTerms(request.paymentTerms())
                .incoterm(request.incoterm())
                .shippingAddress(request.shippingAddress())
                .billingAddress(request.billingAddress())
                .termsAndConditions(request.termsAndConditions())
                .notes(request.notes())
                .build();

        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase order")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(
            @Parameter(description = "PO ID") @PathVariable UUID id,
            @RequestBody PurchaseOrderRequest request) {

        return purchaseOrderRepository.findById(id)
                .map(order -> {
                    order.setPoNo(request.poNo());
                    order.setPoDate(request.poDate());
                    order.setExpectedDeliveryDate(request.expectedDeliveryDate());
                    order.setCurrency(request.currency());
                    order.setExchangeRate(request.exchangeRate());
                    order.setPaymentTerms(request.paymentTerms());
                    order.setIncoterm(request.incoterm());
                    order.setShippingAddress(request.shippingAddress());
                    order.setBillingAddress(request.billingAddress());
                    order.setTermsAndConditions(request.termsAndConditions());
                    order.setNotes(request.notes());

                    PurchaseOrder saved = purchaseOrderRepository.save(order);
                    return ResponseEntity.ok(mapToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve purchase order")
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(
            @Parameter(description = "PO ID") @PathVariable UUID id,
            @Parameter(description = "Approver User ID") @RequestParam UUID approvedBy) {

        return purchaseOrderRepository.findById(id)
                .map(order -> {
                    if (order.getStatus() != PurchaseOrder.POStatus.SUBMITTED) {
                        return ResponseEntity.badRequest().<PurchaseOrderResponse>build();
                    }
                    order.setStatus(PurchaseOrder.POStatus.APPROVED);
                    order.setApprovedBy(approvedBy);
                    order.setApprovedAt(java.time.LocalDateTime.now());

                    PurchaseOrder saved = purchaseOrderRepository.save(order);
                    return ResponseEntity.ok(mapToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/receipts")
    @Operation(summary = "Get purchase receipts for a PO")
    public ResponseEntity<List<PurchaseReceiptSummary>> getReceipts(
            @Parameter(description = "PO ID") @PathVariable UUID id) {

        List<PurchaseReceipt> receipts = purchaseReceiptRepository.findByPurchaseOrderId(id);
        return ResponseEntity.ok(receipts.stream().map(this::mapReceiptToSummary).collect(Collectors.toList()));
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
        List<PurchaseOrderItemResponse> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId())
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        BigDecimal totalQty = items.stream().map(PurchaseOrderItemResponse::getQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = items.stream().map(PurchaseOrderItemResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = items.stream().map(PurchaseOrderItemResponse::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = totalAmount.add(taxAmount);

        return PurchaseOrderResponse.of(
                order.getId(),
                order.getCompanyId(),
                order.getPoNo(),
                order.getSupplier().getId(),
                order.getSupplier().getSupplierName(),
                order.getPoDate(),
                order.getExpectedDeliveryDate(),
                order.getStatus().name(),
                order.getCurrency(),
                order.getExchangeRate(),
                totalQty,
                totalAmount,
                taxAmount,
                grandTotal,
                order.getAdvancePaid(),
                order.getPaymentTerms(),
                order.getIncoterm(),
                order.getShippingAddress(),
                order.getBillingAddress(),
                order.getTermsAndConditions(),
                order.getNotes(),
                order.getApprovedBy(),
                order.getApprovedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private PurchaseOrderItemResponse mapItemToResponse(PurchaseOrderItem item) {
        return PurchaseOrderItemResponse.of(
                item.getId(),
                item.getCompanyId(),
                item.getPurchaseOrder().getId(),
                item.getIdx(),
                item.getItem().getId(),
                item.getItem().getCode(),
                item.getItem().getName(),
                item.getItemVariantId(),
                item.getItemVariantId() != null ? item.getItemVariant().getVariantCode() : null,
                item.getQty(),
                item.getReceivedQty(),
                item.getBilledQty(),
                item.getUom(),
                item.getConversionFactor(),
                item.getRate(),
                item.getAmount(),
                item.getDiscountPercent(),
                item.getDiscountAmount(),
                item.getTaxTemplate(),
                item.getTaxAmount(),
                item.getWarehouseId(),
                item.getQty().subtract(item.getReceivedQty()),
                item.getQty().subtract(item.getBilledQty()),
                item.getRequiredDate(),
                item.getDescription(),
                item.getIsSubcontracted(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private PurchaseReceiptSummary mapReceiptToSummary(PurchaseReceipt receipt) {
        return new PurchaseReceiptSummary(
                receipt.getId(),
                receipt.getReceiptNo(),
                receipt.getReceiptDate(),
                receipt.getStatus().name(),
                receipt.getTotalQty(),
                receipt.getTotalAmount()
        );
    }

    public record PurchaseOrderRequest(
            UUID companyId,
            String poNo,
            UUID supplierId,
            LocalDate poDate,
            LocalDate expectedDeliveryDate,
            String currency,
            BigDecimal exchangeRate,
            String paymentTerms,
            String incoterm,
            String shippingAddress,
            String billingAddress,
            String termsAndConditions,
            String notes
    ) {}

    public record PurchaseReceiptSummary(
            UUID id,
            String receiptNo,
            LocalDate receiptDate,
            String status,
            BigDecimal totalQty,
            BigDecimal totalAmount
    ) {}
}