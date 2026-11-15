package com.erp.brazil.inventory.presentation.rest.controller;

import com.erp.brazil.inventory.domain.model.PurchaseOrder;
import com.erp.brazil.inventory.domain.model.PurchaseOrderItem;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderItemRepository;
import com.erp.brazil.inventory.presentation.rest.dto.PurchaseOrderDTO;
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
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Purchase order management APIs")
public class PurchaseOrderController {

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository poiRepository;

    @PostMapping
    @Operation(summary = "Create a new purchase order")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrder po) {
        PurchaseOrder saved = poRepository.save(po);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<PurchaseOrder> getPurchaseOrder(@PathVariable UUID id) {
        return poRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List purchase orders")
    public ResponseEntity<Page<PurchaseOrder>> listPurchaseOrders(
            @RequestParam UUID companyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {

        Page<PurchaseOrder> orders;
        if (status != null) {
            orders = poRepository.findByCompanyIdAndStatus(companyId, PurchaseOrder.POStatus.valueOf(status), pageable);
        } else if (supplierId != null) {
            orders = poRepository.findByCompanyIdAndSupplierId(companyId, supplierId, pageable);
        } else if (fromDate != null && toDate != null) {
            orders = poRepository.findByCompanyIdAndPoDateBetween(companyId, fromDate, toDate, pageable);
        } else {
            orders = Page.empty(pageable);
        }
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase order")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable UUID id, @RequestBody PurchaseOrder po) {
        return poRepository.findById(id)
                .map(existing -> {
                    po.setId(id);
                    return ResponseEntity.ok(poRepository.save(po));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit purchase order for approval")
    public ResponseEntity<PurchaseOrder> submitPurchaseOrder(@PathVariable UUID id) {
        return poRepository.findById(id)
                .map(po -> {
                    po.setStatus(PurchaseOrder.POStatus.SUBMITTED);
                    return ResponseEntity.ok(poRepository.save(po));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve purchase order")
    public ResponseEntity<PurchaseOrder> approvePurchaseOrder(@PathVariable UUID id, @RequestParam UUID approvedBy) {
        return poRepository.findById(id)
                .map(po -> {
                    po.setStatus(PurchaseOrder.POStatus.APPROVED);
                    po.setApprovedBy(approvedBy);
                    po.setApprovedAt(java.time.LocalDateTime.now());
                    return ResponseEntity.ok(poRepository.save(po));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel purchase order")
    public ResponseEntity<PurchaseOrder> cancelPurchaseOrder(@PathVariable UUID id) {
        return poRepository.findById(id)
                .map(po -> {
                    po.setStatus(PurchaseOrder.POStatus.CANCELLED);
                    return ResponseEntity.ok(poRepository.save(po));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get purchase order items")
    public ResponseEntity<List<PurchaseOrderItem>> getItems(@PathVariable UUID id) {
        List<PurchaseOrderItem> items = poiRepository.findByPurchaseOrderIdOrderByIdxAsc(id);
        return ResponseEntity.ok(items);
    }
}