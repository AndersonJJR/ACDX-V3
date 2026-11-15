package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.application.usecase.ReceiveGoodsUseCase;
import com.erp.brazil.inventory.domain.model.PurchaseReceipt;
import com.erp.brazil.inventory.domain.model.PurchaseReceiptItem;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptItemRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderRepository;
import com.erp.brazil.inventory.domain.repository.SupplierRepository;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseReceiptItemResponse;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseReceiptResponse;
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
@RequestMapping("/api/v1/purchase-receipts")
@RequiredArgsConstructor
@Tag(name = "Purchase Receipts", description = "Purchase receipt (goods receipt) management")
public class PurchaseReceiptController {

    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptItemRepository purchaseReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ReceiveGoodsUseCase receiveGoodsUseCase;

    @GetMapping
    @Operation(summary = "List purchase receipts")
    public ResponseEntity<Page<PurchaseReceiptResponse>> listPurchaseReceipts(
            @Parameter(description = "Company ID") @RequestParam UUID companyId,
            @Parameter(description = "Status filter") @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<PurchaseReceipt> receipts;
        if (status != null) {
            receipts = purchaseReceiptRepository.findByCompanyIdAndStatus(
                    companyId, PurchaseReceipt.PRStatus.valueOf(status), pageable);
        } else {
            receipts = purchaseReceiptRepository.findAll(pageable); // Would need company filter
        }

        return ResponseEntity.ok(receipts.map(this::mapToResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase receipt by ID")
    public ResponseEntity<PurchaseReceiptResponse> getPurchaseReceipt(@Parameter(description = "Receipt ID") @PathVariable UUID id) {
        return purchaseReceiptRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new purchase receipt")
    public ResponseEntity<PurchaseReceiptResponse> createPurchaseReceipt(@RequestBody PurchaseReceiptRequest request) {
        var supplierOpt = supplierRepository.findById(request.supplierId());
        if (supplierOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PurchaseOrder purchaseOrder = null;
        if (request.purchaseOrderId() != null) {
            purchaseOrder = purchaseOrderRepository.findById(request.purchaseOrderId()).orElse(null);
        }

        PurchaseReceipt receipt = PurchaseReceipt.create(
                request.companyId(),
                request.receiptNo(),
                supplierOpt.get(),
                purchaseOrder,
                request.receiptDate(),
                request.postingDate(),
                request.currency() != null ? request.currency() : "BRL",
                request.exchangeRate() != null ? request.exchangeRate() : BigDecimal.ONE,
                request.inspectionRequired() != null ? request.inspectionRequired() : false,
                request.transporterName(),
                request.lrNo(),
                request.lrDate(),
                request.vehicleNo(),
                request.shippingAddress(),
                request.notes()
        );

        PurchaseReceipt saved = purchaseReceiptRepository.save(receipt);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase receipt")
    public ResponseEntity<PurchaseReceiptResponse> updatePurchaseReceipt(
            @Parameter(description = "Receipt ID") @PathVariable UUID id,
            @RequestBody PurchaseReceiptRequest request) {

        return purchaseReceiptRepository.findById(id)
                .map(receipt -> {
                    receipt.setReceiptNo(request.receiptNo());
                    receipt.setReceiptDate(request.receiptDate());
                    receipt.setPostingDate(request.postingDate());
                    receipt.setCurrency(request.currency());
                    receipt.setExchangeRate(request.exchangeRate());
                    receipt.setInspectionRequired(request.inspectionRequired());
                    receipt.setTransporterName(request.transporterName());
                    receipt.setLrNo(request.lrNo());
                    receipt.setLrDate(request.lrDate());
                    receipt.setVehicleNo(request.vehicleNo());
                    receipt.setNotes(request.notes());

                    PurchaseReceipt saved = purchaseReceiptRepository.save(receipt);
                    return ResponseEntity.ok(mapToResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive goods (trigger stock update)")
    public ResponseEntity<ReceiveGoodsUseCase.Output> receiveGoods(
            @Parameter(description = "Receipt ID") @PathVariable UUID id,
            @Parameter(description = "User ID") @RequestParam UUID userId) {

        try {
            ReceiveGoodsUseCase.Input input = new ReceiveGoodsUseCase.Input(id, userId);
            ReceiveGoodsUseCase.Output output = receiveGoodsUseCase.execute(input);
            return ResponseEntity.ok(output);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private PurchaseReceiptResponse mapToResponse(PurchaseReceipt receipt) {
        List<PurchaseReceiptItemResponse> items = purchaseReceiptItemRepository.findByPurchaseReceiptId(receipt.getId())
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        BigDecimal totalQty = items.stream().map(PurchaseReceiptItemResponse::getQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = items.stream().map(PurchaseReceiptItemResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = items.stream().map(PurchaseReceiptItemResponse::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = totalAmount.add(taxAmount);

        return PurchaseReceiptResponse.of(
                receipt.getId(),
                receipt.getCompanyId(),
                receipt.getReceiptNo(),
                receipt.getSupplier().getId(),
                receipt.getSupplier().getSupplierName(),
                receipt.getPurchaseOrder() != null ? receipt.getPurchaseOrder().getId() : null,
                receipt.getPurchaseOrder() != null ? receipt.getPurchaseOrder().getPoNo() : null,
                receipt.getReceiptDate(),
                receipt.getPostingDate(),
                receipt.getStatus().name(),
                receipt.getCurrency(),
                receipt.getExchangeRate(),
                totalQty,
                totalAmount,
                taxAmount,
                grandTotal,
                receipt.getInspectionRequired(),
                receipt.getTransporterName(),
                receipt.getLrNo(),
                receipt.getLrDate(),
                receipt.getVehicleNo(),
                receipt.getNotes(),
                receipt.getPerBilled(),
                receipt.getCreatedAt(),
                receipt.getUpdatedAt(),
                items
        );
    }

    private PurchaseReceiptItemResponse mapItemToResponse(PurchaseReceiptItem item) {
        return PurchaseReceiptItemResponse.of(
                item.getId(),
                item.getCompanyId(),
                item.getPurchaseReceipt().getId(),
                item.getIdx(),
                item.getPurchaseOrderItemId(),
                item.getItem().getId(),
                item.getItem().getCode(),
                item.getItem().getName(),
                item.getItemVariantId(),
                item.getItemVariantId() != null ? item.getItemVariant().getVariantCode() : null,
                item.getQty(),
                item.getRejectedQty(),
                item.getUom(),
                item.getConversionFactor(),
                item.getRate(),
                item.getAmount(),
                item.getWarehouseId(),
                item.getWarehouseId() != null ? "" : null,
                item.getLocationId(),
                item.getLocationId() != null ? "" : null,
                item.getBatchNo(),
                item.getSerialNos(),
                item.getQualityStatus().name(),
                item.getManufacturingDate(),
                item.getExpiryDate(),
                item.getDescription(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public record PurchaseReceiptRequest(
            UUID companyId,
            String receiptNo,
            UUID supplierId,
            UUID purchaseOrderId,
            LocalDate receiptDate,
            LocalDate postingDate,
            String currency,
            BigDecimal exchangeRate,
            Boolean inspectionRequired,
            String transporterName,
            String lrNo,
            LocalDate lrDate,
            String vehicleNo,
            String shippingAddress,
            String notes
    ) {}
}