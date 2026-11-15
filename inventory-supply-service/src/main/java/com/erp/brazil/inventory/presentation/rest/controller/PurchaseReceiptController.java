package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.application.usecase.ReceiveGoodsUseCase;
import com.erp.brazil.inventory.domain.model.PurchaseReceipt;
import com.erp.brazil.inventory.domain.model.PurchaseReceiptItem;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptItemRepository;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseReceiptDTO;
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
@RequestMapping("/api/v1/purchase-receipts")
@RequiredArgsConstructor
@Tag(name = "Purchase Receipts", description = "Purchase receipt management APIs")
public class PurchaseReceiptController {

    private final PurchaseReceiptRepository prRepository;
    private final PurchaseReceiptItemRepository priRepository;
    private final ReceiveGoodsUseCase receiveGoodsUseCase;

    @PostMapping
    @Operation(summary = "Create a new purchase receipt")
    public ResponseEntity<PurchaseReceipt> createPurchaseReceipt(@RequestBody PurchaseReceipt pr) {
        PurchaseReceipt saved = prRepository.save(pr);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase receipt by ID")
    public ResponseEntity<PurchaseReceipt> getPurchaseReceipt(@PathVariable UUID id) {
        return prRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List purchase receipts")
    public ResponseEntity<Page<PurchaseReceipt>> listPurchaseReceipts(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID purchaseOrderId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {

        Page<PurchaseReceipt> receipts;
        if (status != null) {
            receipts = prRepository.findByCompanyIdAndStatus(companyId, PurchaseReceipt.PRStatus.valueOf(status), pageable);
        } else if (supplierId != null) {
            receipts = prRepository.findByCompanyIdAndSupplierId(companyId, supplierId, pageable);
        } else if (purchaseOrderId != null) {
            receipts = prRepository.findByPurchaseOrderId(purchaseOrderId, pageable);
        } else if (fromDate != null && toDate != null) {
            receipts = prRepository.findByCompanyIdAndReceiptDateBetween(companyId, fromDate, toDate, pageable);
        } else {
            receipts = Page.empty(pageable);
        }
        return ResponseEntity.ok(receipts);
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive goods against purchase receipt")
    public ResponseEntity<PurchaseReceipt> receiveGoods(@PathVariable UUID id, @RequestBody List<ReceiveGoodsUseCase.ReceiveItemRequest> items) {
        PurchaseReceipt receipt = receiveGoodsUseCase.receiveGoods(
                prRepository.findById(id).map(PurchaseReceipt::getCompanyId).orElseThrow(), id, items);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit purchase receipt")
    public ResponseEntity<PurchaseReceipt> submitPurchaseReceipt(@PathVariable UUID id) {
        return prRepository.findById(id)
                .map(pr -> {
                    pr.setStatus(PurchaseReceipt.PRStatus.SUBMITTED);
                    return ResponseEntity.ok(prRepository.save(pr));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/inspect")
    @Operation(summary = "Record quality inspection")
    public ResponseEntity<PurchaseReceipt> inspectPurchaseReceipt(@PathVariable UUID id, @RequestParam UUID inspectedBy, @RequestParam String qualityStatus) {
        return prRepository.findById(id)
                .map(pr -> {
                    pr.setStatus(PurchaseReceipt.PRStatus.QUALITY_INSPECTION);
                    pr.setInspectionBy(inspectedBy);
                    pr.setInspectionDate(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(prRepository.save(pr));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get purchase receipt items")
    public ResponseEntity<List<PurchaseReceiptItem>> getItems(@PathVariable UUID id) {
        List<PurchaseReceiptItem> items = priRepository.findByPurchaseReceiptIdOrderByIdxAsc(id);
        return ResponseEntity.ok(items);
    }
}