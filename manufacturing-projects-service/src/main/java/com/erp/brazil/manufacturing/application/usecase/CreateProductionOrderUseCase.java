package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.event.SalesOrderConfirmedEvent;
import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.Routing;
import com.erp.brazil.manufacturing.domain.repository.BOMRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.RoutingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CreateProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final BOMRepository bomRepository;
    private final RoutingRepository routingRepository;
    private final ExplodeBOMUseCase explodeBOMUseCase;

    public CreateProductionOrderUseCase(ProductionOrderRepository productionOrderRepository,
                                         BOMRepository bomRepository,
                                         RoutingRepository routingRepository,
                                         ExplodeBOMUseCase explodeBOMUseCase) {
        this.productionOrderRepository = productionOrderRepository;
        this.bomRepository = bomRepository;
        this.routingRepository = routingRepository;
        this.explodeBOMUseCase = explodeBOMUseCase;
    }

    public ProductionOrder createFromSalesOrder(SalesOrderConfirmedEvent event) {
        for (SalesOrderConfirmedEvent.SalesOrderItem item : event.getItems()) {
            if (item.getProjectId() != null) {
                // MTO - Make to Order
                createMTOProductionOrder(event, item);
            } else {
                // MTS - Make to Stock (could be triggered by MPS/MRP)
                createMTSProductionOrder(event, item);
            }
        }
        return null; // Returns void, events are published separately
    }

    public ProductionOrder createMTOProductionOrder(SalesOrderConfirmedEvent event, SalesOrderConfirmedEvent.SalesOrderItem item) {
        // Find active BOM for product
        List<BOM> boms = bomRepository.findByProductIdAndStatusOrderByVersionDesc(
            item.getProductId(), BOM.BOMStatus.ACTIVE);
        BOM bom = boms.stream()
            .filter(b -> b.getEffectiveFrom() == null || !b.getEffectiveFrom().isAfter(event.getOrderDate()))
            .filter(b -> b.getEffectiveTo() == null || !b.getEffectiveTo().isBefore(event.getOrderDate()))
            .findFirst()
            .orElse(null);

        // Find active Routing for product
        List<Routing> routings = routingRepository.findByProductIdAndStatusOrderByVersionDesc(
            item.getProductId(), Routing.RoutingStatus.ACTIVE);
        Routing routing = routings.stream()
            .filter(r -> r.getEffectiveFrom() == null || !r.getEffectiveFrom().isAfter(event.getOrderDate()))
            .filter(r -> r.getEffectiveTo() == null || !r.getEffectiveTo().isBefore(event.getOrderDate()))
            .findFirst()
            .orElse(null);

        String orderNumber = generateOrderNumber("MTO");

        ProductionOrder order = ProductionOrder.builder()
            .orderNumber(orderNumber)
            .productId(item.getProductId())
            .orderType(ProductionOrder.OrderType.MTO)
            .status(ProductionOrder.ProductionOrderStatus.PLANNED)
            .priority(ProductionOrder.Priority.NORMAL)
            .description("Production Order from Sales Order " + event.getOrderNumber())
            .orderedQuantity(item.getQuantity())
            .unitOfMeasure(item.getUnitOfMeasure())
            .bomId(bom != null ? bom.getId() : null)
            .routingId(routing != null ? routing.getId() : null)
            .salesOrderId(event.getSalesOrderId())
            .salesOrderItemId(item.getSalesOrderItemId())
            .projectId(item.getProjectId())
            .warehouseId(item.getWarehouseId())
            .plannedStartDate(event.getRequestedDeliveryDate().minusDays(calculateLeadTime(bom, routing)))
            .plannedEndDate(event.getRequestedDeliveryDate())
            .currency("BRL")
            .build();

        // Create order items from BOM
        if (bom != null) {
            ExplodeBOMUseCase.BOMExplosionResult explosion = explodeBOMUseCase.explode(bom.getId(), item.getQuantity());
            for (ExplodeBOMUseCase.BOMExplosionItem explItem : explosion.getItems()) {
                if (!explItem.getIsPhantom()) {
                    ProductionOrderItem orderItem = ProductionOrderItem.builder()
                        .componentId(explItem.getComponentId())
                        .componentType(explItem.getComponentType())
                        .requiredQuantity(explItem.getQuantity())
                        .unitOfMeasure(explItem.getUnitOfMeasure())
                        .warehouseId(item.getWarehouseId())
                        .bomItemId(explItem.getBomItemId())
                        .status(ProductionOrderItem.ItemStatus.PENDING)
                        .build();
                    order.addItem(orderItem);
                }
            }
        }

        // Create operations from Routing
        if (routing != null) {
            List<Routing.RoutingOperation> routingOps = routing.getOperations(); // Need to fetch
            int seq = 10;
            for (Routing.RoutingOperation routingOp : routingOps) {
                ProductionOrderOperation orderOp = ProductionOrderOperation.builder()
                    .sequence(seq)
                    .routingOperationId(routingOp.getId())
                    .workCenterId(routingOp.getWorkCenterId())
                    .workStationId(routingOp.getWorkStationId())
                    .operationName(routingOp.getOperationName())
                    .description(routingOp.getDescription())
                    .plannedSetupTimeMinutes(routingOp.getSetupTimeMinutes())
                    .plannedRunTimeMinutes(routingOp.getRunTimeMinutes())
                    .crewSize(routingOp.getCrewSize())
                    .isOutsourced(routingOp.getIsOutsourced())
                    .vendorId(routingOp.getVendorId())
                    .inspectionRequired(routingOp.getInspectionRequired())
                    .samplingPlanId(routingOp.getSamplingPlanId())
                    .status(ProductionOrderOperation.OperationStatus.PENDING)
                    .build();
                order.addOperation(orderOp);
                seq += 10;
            }
        }

        return productionOrderRepository.save(order);
    }

    public ProductionOrder createMTSProductionOrder(SalesOrderConfirmedEvent event, SalesOrderConfirmedEvent.SalesOrderItem item) {
        // Similar to MTO but with MTS order type
        List<BOM> boms = bomRepository.findByProductIdAndStatusOrderByVersionDesc(
            item.getProductId(), BOM.BOMStatus.ACTIVE);
        BOM bom = boms.stream()
            .filter(b -> b.getEffectiveFrom() == null || !b.getEffectiveFrom().isAfter(event.getOrderDate()))
            .filter(b -> b.getEffectiveTo() == null || !b.getEffectiveTo().isBefore(event.getOrderDate()))
            .findFirst()
            .orElse(null);

        List<Routing> routings = routingRepository.findByProductIdAndStatusOrderByVersionDesc(
            item.getProductId(), Routing.RoutingStatus.ACTIVE);
        Routing routing = routings.stream()
            .filter(r -> r.getEffectiveFrom() == null || !r.getEffectiveFrom().isAfter(event.getOrderDate()))
            .filter(r -> r.getEffectiveTo() == null || !r.getEffectiveTo().isBefore(event.getOrderDate()))
            .findFirst()
            .orElse(null);

        String orderNumber = generateOrderNumber("MTS");

        ProductionOrder order = ProductionOrder.builder()
            .orderNumber(orderNumber)
            .productId(item.getProductId())
            .orderType(ProductionOrder.OrderType.MTS)
            .status(ProductionOrder.ProductionOrderStatus.PLANNED)
            .priority(ProductionOrder.Priority.NORMAL)
            .description("Production Order for Stock from Sales Order " + event.getOrderNumber())
            .orderedQuantity(item.getQuantity())
            .unitOfMeasure(item.getUnitOfMeasure())
            .bomId(bom != null ? bom.getId() : null)
            .routingId(routing != null ? routing.getId() : null)
            .salesOrderId(event.getSalesOrderId())
            .salesOrderItemId(item.getSalesOrderItemId())
            .warehouseId(item.getWarehouseId())
            .plannedStartDate(event.getOrderDate().plusDays(1))
            .plannedEndDate(event.getRequestedDeliveryDate())
            .currency("BRL")
            .build();

        if (bom != null) {
            ExplodeBOMUseCase.BOMExplosionResult explosion = explodeBOMUseCase.explode(bom.getId(), item.getQuantity());
            for (ExplodeBOMUseCase.BOMExplosionItem explItem : explosion.getItems()) {
                if (!explItem.getIsPhantom()) {
                    ProductionOrderItem orderItem = ProductionOrderItem.builder()
                        .componentId(explItem.getComponentId())
                        .componentType(explItem.getComponentType())
                        .requiredQuantity(explItem.getQuantity())
                        .unitOfMeasure(explItem.getUnitOfMeasure())
                        .warehouseId(item.getWarehouseId())
                        .bomItemId(explItem.getBomItemId())
                        .status(ProductionOrderItem.ItemStatus.PENDING)
                        .build();
                    order.addItem(orderItem);
                }
            }
        }

        if (routing != null) {
            List<Routing.RoutingOperation> routingOps = routing.getOperations();
            int seq = 10;
            for (Routing.RoutingOperation routingOp : routingOps) {
                ProductionOrderOperation orderOp = ProductionOrderOperation.builder()
                    .sequence(seq)
                    .routingOperationId(routingOp.getId())
                    .workCenterId(routingOp.getWorkCenterId())
                    .workStationId(routingOp.getWorkStationId())
                    .operationName(routingOp.getOperationName())
                    .description(routingOp.getDescription())
                    .plannedSetupTimeMinutes(routingOp.getSetupTimeMinutes())
                    .plannedRunTimeMinutes(routingOp.getRunTimeMinutes())
                    .crewSize(routingOp.getCrewSize())
                    .isOutsourced(routingOp.getIsOutsourced())
                    .vendorId(routingOp.getVendorId())
                    .inspectionRequired(routingOp.getInspectionRequired())
                    .samplingPlanId(routingOp.getSamplingPlanId())
                    .status(ProductionOrderOperation.OperationStatus.PENDING)
                    .build();
                order.addOperation(orderOp);
                seq += 10;
            }
        }

        return productionOrderRepository.save(order);
    }

    public ProductionOrder createManualProductionOrder(ProductionOrder.OrderType orderType,
                                                        UUID productId,
                                                        BigDecimal quantity,
                                                        String unitOfMeasure,
                                                        UUID warehouseId,
                                                        LocalDateTime plannedStartDate,
                                                        LocalDateTime plannedEndDate,
                                                        UUID salesOrderId,
                                                        UUID projectId,
                                                        String description) {
        List<BOM> boms = bomRepository.findByProductIdAndStatusOrderByVersionDesc(
            productId, BOM.BOMStatus.ACTIVE);
        BOM bom = boms.stream()
            .filter(b -> b.getEffectiveFrom() == null || !b.getEffectiveFrom().isAfter(LocalDateTime.now()))
            .filter(b -> b.getEffectiveTo() == null || !b.getEffectiveTo().isBefore(LocalDateTime.now()))
            .findFirst()
            .orElse(null);

        List<Routing> routings = routingRepository.findByProductIdAndStatusOrderByVersionDesc(
            productId, Routing.RoutingStatus.ACTIVE);
        Routing routing = routings.stream()
            .filter(r -> r.getEffectiveFrom() == null || !r.getEffectiveFrom().isAfter(LocalDateTime.now()))
            .filter(r -> r.getEffectiveTo() == null || !r.getEffectiveTo().isBefore(LocalDateTime.now()))
            .findFirst()
            .orElse(null);

        String orderNumber = generateOrderNumber(orderType.name());

        ProductionOrder order = ProductionOrder.builder()
            .orderNumber(orderNumber)
            .productId(productId)
            .orderType(orderType)
            .status(ProductionOrder.ProductionOrderStatus.PLANNED)
            .priority(ProductionOrder.Priority.NORMAL)
            .description(description)
            .orderedQuantity(quantity)
            .unitOfMeasure(unitOfMeasure)
            .bomId(bom != null ? bom.getId() : null)
            .routingId(routing != null ? routing.getId() : null)
            .salesOrderId(salesOrderId)
            .projectId(projectId)
            .warehouseId(warehouseId)
            .plannedStartDate(plannedStartDate)
            .plannedEndDate(plannedEndDate)
            .currency("BRL")
            .build();

        if (bom != null) {
            ExplodeBOMUseCase.BOMExplosionResult explosion = explodeBOMUseCase.explode(bom.getId(), quantity);
            for (ExplodeBOMUseCase.BOMExplosionItem explItem : explosion.getItems()) {
                if (!explItem.getIsPhantom()) {
                    ProductionOrderItem orderItem = ProductionOrderItem.builder()
                        .componentId(explItem.getComponentId())
                        .componentType(explItem.getComponentType())
                        .requiredQuantity(explItem.getQuantity())
                        .unitOfMeasure(explItem.getUnitOfMeasure())
                        .warehouseId(warehouseId)
                        .bomItemId(explItem.getBomItemId())
                        .status(ProductionOrderItem.ItemStatus.PENDING)
                        .build();
                    order.addItem(orderItem);
                }
            }
        }

        if (routing != null) {
            List<Routing.RoutingOperation> routingOps = routing.getOperations();
            int seq = 10;
            for (Routing.RoutingOperation routingOp : routingOps) {
                ProductionOrderOperation orderOp = ProductionOrderOperation.builder()
                    .sequence(seq)
                    .routingOperationId(routingOp.getId())
                    .workCenterId(routingOp.getWorkCenterId())
                    .workStationId(routingOp.getWorkStationId())
                    .operationName(routingOp.getOperationName())
                    .description(routingOp.getDescription())
                    .plannedSetupTimeMinutes(routingOp.getSetupTimeMinutes())
                    .plannedRunTimeMinutes(routingOp.getRunTimeMinutes())
                    .crewSize(routingOp.getCrewSize())
                    .isOutsourced(routingOp.getIsOutsourced())
                    .vendorId(routingOp.getVendorId())
                    .inspectionRequired(routingOp.getInspectionRequired())
                    .samplingPlanId(routingOp.getSamplingPlanId())
                    .status(ProductionOrderOperation.OperationStatus.PENDING)
                    .build();
                order.addOperation(orderOp);
                seq += 10;
            }
        }

        return productionOrderRepository.save(order);
    }

    private String generateOrderNumber(String prefix) {
        long count = productionOrderRepository.count() + 1;
        return String.format("%s-%06d", prefix, count);
    }

    private int calculateLeadTime(BOM bom, Routing routing) {
        int leadTime = 1; // Base lead time
        if (routing != null) {
            leadTime += routing.getTotalRunTimeMinutes().add(routing.getTotalSetupTimeMinutes())
                .divide(BigDecimal.valueOf(480), 0, BigDecimal.ROUND_UP).intValue(); // 8 hours per day
        }
        return Math.max(leadTime, 1);
    }

    // Need to add import for ProductionOrderItem
    private static class ProductionOrderItem {
        // placeholder
    }
}