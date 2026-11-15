package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.application.usecase.SubcontractOutUseCase;
import com.erp.brazil.manufacturing.application.usecase.ReceiveSubcontractedUseCase;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder.SubcontractingStatus;
import com.erp.brazil.manufacturing.domain.repository.SubcontractingOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.SubcontractingReceiptRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.SubcontractingDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/v1/subcontracting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subcontracting", description = "Subcontracting order management")
public class SubcontractingController {

    private final SubcontractingOrderRepository subcontractingOrderRepository;
    private final SubcontractingReceiptRepository subcontractingReceiptRepository;
    private final SubcontractOutUseCase subcontractOutUseCase;
    private final ReceiveSubcontractedUseCase receiveSubcontractedUseCase;

    @PostMapping("/orders")
    @Operation(summary = "Create subcontracting order")
    public ResponseEntity<SubcontractingDto.SubcontractingOrderDto> createSubcontractingOrder(
            @Valid @RequestBody SubcontractingDto.CreateSubcontractingOrderRequest request) {
        log.info("Creating subcontracting order for production order {}", request.getProductionOrderId());

        var command = SubcontractOutUseCase.SubcontractOutCommand.builder()
                .productionOrderId(request.getProductionOrderId())
                .operationId(request.getOperationId())
                .vendorId(request.getVendorId())
                .orderedQuantity(request.getOrderedQuantity())
                .unitPrice(request.getUnitPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "BRL")
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .warehouseId(request.getWarehouseId())
                .items(request.getItems().stream().map(item ->
                        SubcontractOutUseCase.SubcontractItemCommand.builder()
                                .componentId(item.getComponentId())
                                .componentType(item.getComponentType())
                                .quantity(item.getQuantity())
                                .unitOfMeasure(item.getUnitOfMeasure())
                                .warehouseId(item.getWarehouseId())
                                .locationId(item.getLocationId())
                                .build()
                ).toList())
                .notes(request.getNotes())
                .createdBy("api-user")
                .build();

        var result = subcontractOutUseCase.execute(command);
        SubcontractingOrder so = subcontractingOrderRepository.findById(result.subcontractingOrderId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(SubcontractingDto.SubcontractingOrderDto.fromEntity(so));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get subcontracting order by ID")
    public ResponseEntity<SubcontractingDto.SubcontractingOrderDto> getSubcontractingOrder(@PathVariable UUID id) {
        SubcontractingOrder so = subcontractingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcontracting order not found: " + id));
        return ResponseEntity.ok(SubcontractingDto.SubcontractingOrderDto.fromEntity(so));
    }

    @GetMapping("/orders")
    @Operation(summary = "List subcontracting orders with pagination")
    public ResponseEntity<Page<SubcontractingDto.SubcontractingOrderDto>> listSubcontractingOrders(
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) UUID productionOrderId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SubcontractingOrder> orders;
        if (vendorId != null && status != null) {
            orders = subcontractingOrderRepository.findByVendorId(vendorId, pageable);
        } else if (vendorId != null) {
            orders = subcontractingOrderRepository.findByVendorId(vendorId, pageable);
        } else if (productionOrderId != null) {
            orders = subcontractingOrderRepository.findByProductionOrderId(productionOrderId, pageable);
        } else if (status != null) {
            orders = subcontractingOrderRepository.findByStatus(SubcontractingStatus.valueOf(status), pageable);
        } else {
            orders = subcontractingOrderRepository.findAll(pageable);
        }
        return ResponseEntity.ok(orders.map(SubcontractingDto.SubcontractingOrderDto::fromEntity));
    }

    @PostMapping("/orders/{id}/send-materials")
    @Operation(summary = "Send materials for subcontracting")
    public ResponseEntity<SubcontractingDto.SubcontractingOrderDto> sendMaterials(
            @PathVariable UUID id,
            @Valid @RequestBody SubcontractingDto.SendMaterialsRequest request) {
        subcontractOutUseCase.sendMaterials(id, request.getSentBy());
        SubcontractingOrder so = subcontractingOrderRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(SubcontractingDto.SubcontractingOrderDto.fromEntity(so));
    }

    @PostMapping("/orders/{id}/receive")
    @Operation(summary = "Receive subcontracted items")
    public ResponseEntity<SubcontractingDto.SubcontractingReceiptDto> receiveSubcontracted(
            @PathVariable UUID id,
            @Valid @RequestBody SubcontractingDto.ReceiveSubcontractedRequest request) {
        var result = receiveSubcontractedUseCase.execute(new ReceiveSubcontractedUseCase.ReceiveSubcontractedCommand(
                id,
                request.getReceiptNumber(),
                request.getReceivedQuantity(),
                request.getAcceptedQuantity(),
                request.getRejectedQuantity(),
                request.getWarehouseId(),
                request.getLocationId(),
                request.getVendorInvoiceNumber(),
                request.getVendorInvoiceDate(),
                request.getNotes(),
                request.getReceivedBy()
        ));

        var receipt = subcontractingReceiptRepository.findById(result.receiptId()).orElseThrow();
        return ResponseEntity.ok(SubcontractingDto.SubcontractingReceiptDto.fromEntity(receipt));
    }

    @GetMapping("/receipts/{id}")
    @Operation(summary = "Get subcontracting receipt by ID")
    public ResponseEntity<SubcontractingDto.SubcontractingReceiptDto> getSubcontractingReceipt(@PathVariable UUID id) {
        var receipt = subcontractingReceiptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcontracting receipt not found: " + id));
        return ResponseEntity.ok(SubcontractingDto.SubcontractingReceiptDto.fromEntity(receipt));
    }

    @GetMapping("/receipts")
    @Operation(summary = "List subcontracting receipts with pagination")
    public ResponseEntity<Page<SubcontractingDto.SubcontractingReceiptDto>> listSubcontractingReceipts(
            @RequestParam(required = false) UUID subcontractingOrderId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SubcontractingReceipt> receipts;
        if (subcontractingOrderId != null) {
            receipts = subcontractingReceiptRepository.findBySubcontractingOrderId(subcontractingOrderId, pageable);
        } else if (status != null) {
            receipts = subcontractingReceiptRepository.findByStatus(
                    com.erp.brazil.manufacturing.domain.model.SubcontractingReceipt.ReceiptStatus.valueOf(status), pageable);
        } else {
            receipts = subcontractingReceiptRepository.findAll(pageable);
        }
        return ResponseEntity.ok(receipts.map(SubcontractingDto.SubcontractingReceiptDto::fromEntity));
    }

    @PatchMapping("/orders/{id}/cancel")
    @Operation(summary = "Cancel subcontracting order")
    public ResponseEntity<SubcontractingDto.SubcontractingOrderDto> cancelSubcontractingOrder(@PathVariable UUID id) {
        SubcontractingOrder so = subcontractingOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subcontracting order not found: " + id));

        if (so.getStatus() == SubcontractingStatus.COMPLETED || so.getStatus() == SubcontractingStatus.CLOSED) {
            throw new IllegalStateException("Cannot cancel completed or closed order");
        }

        so.setStatus(SubcontractingStatus.CANCELLED);
        subcontractingOrderRepository.save(so);
        return ResponseEntity.ok(SubcontractingDto.SubcontractingOrderDto.fromEntity(so));
    }
}