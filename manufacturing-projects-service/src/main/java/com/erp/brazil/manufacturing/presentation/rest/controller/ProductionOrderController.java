package com.erp.brazil.manufacturing.presentation.rest.controller;

import com.erp.brazil.manufacturing.application.usecase.CreateProductionOrderUseCase;
import com.erp.brazil.manufacturing.application.usecase.ReleaseProductionOrderUseCase;
import com.erp.brazil.manufacturing.application.usecase.ReportProductionUseCase;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.ProductionOrderStatus;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.OrderType;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.Priority;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.presentation.rest.dto.ProductionOrderDto;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/production-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Production Orders", description = "Production order management")
public class ProductionOrderController {

    private final ProductionOrderRepository productionOrderRepository;
    private final CreateProductionOrderUseCase createProductionOrderUseCase;
    private final ReleaseProductionOrderUseCase releaseProductionOrderUseCase;
    private final ReportProductionUseCase reportProductionUseCase;

    @PostMapping
    @Operation(summary = "Create a new production order")
    public ResponseEntity<ProductionOrderDto> createProductionOrder(@Valid @RequestBody ProductionOrderDto.CreateProductionOrderRequest request) {
        log.info("Creating production order for product {}", request.getProductId());

        var command = CreateProductionOrderUseCase.CreateProductionOrderCommand.builder()
                .productId(request.getProductId())
                .orderedQuantity(request.getOrderedQuantity())
                .unitOfMeasure(request.getUnitOfMeasure())
                .orderType(OrderType.valueOf(request.getOrderType()))
                .priority(Priority.valueOf(request.getPriority() != null ? request.getPriority() : "NORMAL"))
                .salesOrderId(request.getSalesOrderId())
                .salesOrderItemId(request.getSalesOrderItemId())
                .projectId(request.getProjectId())
                .warehouseId(request.getWarehouseId())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .description(request.getDescription())
                .notes(request.getNotes())
                .createdBy("api-user")
                .build();

        var result = createProductionOrderUseCase.execute(command);
        ProductionOrder po = productionOrderRepository.findById(result.productionOrderId()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductionOrderDto.fromEntity(po));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get production order by ID")
    public ResponseEntity<ProductionOrderDto> getProductionOrder(@PathVariable UUID id) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + id));
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @GetMapping
    @Operation(summary = "List production orders with pagination")
    public ResponseEntity<Page<ProductionOrderDto>> listProductionOrders(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID salesOrderId,
            @RequestParam(required = false) UUID projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductionOrder> orders;
        if (productId != null && status != null) {
            orders = productionOrderRepository.findByProductIdAndStatus(productId, ProductionOrderStatus.valueOf(status), pageable);
        } else if (productId != null) {
            orders = productionOrderRepository.findByProductId(productId, pageable);
        } else if (status != null) {
            orders = productionOrderRepository.findByStatus(ProductionOrderStatus.valueOf(status), pageable);
        } else if (salesOrderId != null) {
            orders = productionOrderRepository.findBySalesOrderId(salesOrderId, pageable);
        } else if (projectId != null) {
            orders = productionOrderRepository.findByProjectId(projectId, pageable);
        } else {
            orders = productionOrderRepository.findAll(pageable);
        }
        return ResponseEntity.ok(orders.map(ProductionOrderDto::fromEntity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update production order (only if planned)")
    public ResponseEntity<ProductionOrderDto> updateProductionOrder(@PathVariable UUID id, @Valid @RequestBody ProductionOrderDto.CreateProductionOrderRequest request) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + id));

        if (po.getStatus() != ProductionOrderStatus.PLANNED) {
            throw new IllegalStateException("Can only update planned production orders");
        }

        po.setPriority(Priority.valueOf(request.getPriority()));
        po.setPlannedStartDate(request.getPlannedStartDate());
        po.setPlannedEndDate(request.getPlannedEndDate());
        po.setDescription(request.getDescription());
        po.setNotes(request.getNotes());
        po.setWarehouseId(request.getWarehouseId());

        productionOrderRepository.save(po);
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release production order")
    public ResponseEntity<ProductionOrderDto> releaseProductionOrder(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionOrderDto.ReleaseProductionOrderRequest request) {
        var result = releaseProductionOrderUseCase.execute(new ReleaseProductionOrderUseCase.ReleaseProductionOrderCommand(
                id, request.getReleasedBy()
        ));
        ProductionOrder po = productionOrderRepository.findById(result.productionOrderId()).orElseThrow();
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @PostMapping("/{id}/report-production")
    @Operation(summary = "Report production output")
    public ResponseEntity<ProductionOrderDto> reportProduction(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionOrderDto.ReportProductionRequest request) {
        var result = reportProductionUseCase.execute(new ReportProductionUseCase.ReportProductionCommand(
                id,
                request.getProducedQuantity(),
                request.getScrappedQuantity(),
                request.getNotes(),
                request.getReportedBy()
        ));
        ProductionOrder po = productionOrderRepository.findById(result.productionOrderId()).orElseThrow();
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @PatchMapping("/{id}/hold")
    @Operation(summary = "Put production order on hold")
    public ResponseEntity<ProductionOrderDto> holdProductionOrder(@PathVariable UUID id) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + id));

        if (po.getStatus() != ProductionOrderStatus.RELEASED && po.getStatus() != ProductionOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only hold released or in-progress orders");
        }

        po.setStatus(ProductionOrderStatus.ON_HOLD);
        productionOrderRepository.save(po);
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @PatchMapping("/{id}/resume")
    @Operation(summary = "Resume production order from hold")
    public ResponseEntity<ProductionOrderDto> resumeProductionOrder(@PathVariable UUID id) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + id));

        if (po.getStatus() != ProductionOrderStatus.ON_HOLD) {
            throw new IllegalStateException("Can only resume orders on hold");
        }

        po.setStatus(ProductionOrderStatus.RELEASED);
        productionOrderRepository.save(po);
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel production order")
    public ResponseEntity<ProductionOrderDto> cancelProductionOrder(@PathVariable UUID id) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + id));

        if (po.isCompleted()) {
            throw new IllegalStateException("Cannot cancel completed order");
        }

        po.setStatus(ProductionOrderStatus.CANCELLED);
        productionOrderRepository.save(po);
        return ResponseEntity.ok(ProductionOrderDto.fromEntity(po));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete production order (only if planned)")
    public ResponseEntity<Void> deleteProductionOrder(@PathVariable UUID id) {
        ProductionOrder po = productionOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + id));

        if (po.getStatus() != ProductionOrderStatus.PLANNED) {
            throw new IllegalStateException("Can only delete planned production orders");
        }

        productionOrderRepository.delete(po);
        return ResponseEntity.noContent().build();
    }
}