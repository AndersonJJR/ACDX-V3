package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.application.event.ProductionOrderEventPublisher;
import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.model.BOM.BOMStatus;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.ProductionOrderStatus;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.OrderType;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.Priority;
import com.erp.brazil.manufacturing.domain.model.Routing;
import com.erp.brazil.manufacturing.domain.model.Routing.RoutingStatus;
import com.erp.brazil.manufacturing.domain.model.RoutingOperation;
import com.erp.brazil.manufacturing.domain.repository.BOMRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.RoutingRepository;
import com.erp.brazil.manufacturing.domain.repository.RoutingOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final BOMRepository bomRepository;
    private final RoutingRepository routingRepository;
    private final RoutingOperationRepository routingOperationRepository;
    private final ProductionOrderEventPublisher eventPublisher;
    private final ExplodeBOMUseCase explodeBOMUseCase;

    public record CreateProductionOrderCommand(
            UUID productId,
            BigDecimal orderedQuantity,
            String unitOfMeasure,
            OrderType orderType,
            Priority priority,
            UUID salesOrderId,
            UUID salesOrderItemId,
            UUID projectId,
            UUID warehouseId,
            LocalDateTime plannedStartDate,
            LocalDateTime plannedEndDate,
            String description,
            String notes,
            String createdBy
    ) {}

    public record CreateProductionOrderResult(
            UUID productionOrderId,
            String orderNumber
    ) {}

    @Transactional
    public CreateProductionOrderResult execute(CreateProductionOrderCommand command) {
        log.info("Creating production order for product {} qty {}", command.productId(), command.orderedQuantity());

        BOM bom = bomRepository.findByProductIdAndStatusOrderByVersionDesc(command.productId(), BOMStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No active BOM found for product: " + command.productId()));

        Routing routing = routingRepository.findByProductIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                command.productId(), RoutingStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now().plusYears(10))
                .orElse(null);

        String orderNumber = generateOrderNumber(command.orderType());

        ProductionOrder productionOrder = ProductionOrder.builder()
                .orderNumber(orderNumber)
                .productId(command.productId())
                .orderType(command.orderType())
                .status(ProductionOrderStatus.PLANNED)
                .priority(command.priority() != null ? command.priority() : Priority.NORMAL)
                .description(command.description())
                .orderedQuantity(command.orderedQuantity())
                .unitOfMeasure(command.unitOfMeasure())
                .bomId(bom.getId())
                .routingId(routing != null ? routing.getId() : null)
                .salesOrderId(command.salesOrderId())
                .salesOrderItemId(command.salesOrderItemId())
                .projectId(command.projectId())
                .warehouseId(command.warehouseId())
                .plannedStartDate(command.plannedStartDate())
                .plannedEndDate(command.plannedEndDate())
                .notes(command.notes())
                .createdBy(command.createdBy())
                .build();

        explodeAndCreateItems(productionOrder, bom, command.orderedQuantity());

        if (routing != null) {
            createOperationsFromRouting(productionOrder, routing, command.orderedQuantity());
        }

        productionOrderRepository.save(productionOrder);

        eventPublisher.publishProductionOrderCreated(productionOrder);

        log.info("Production order created: {}", orderNumber);
        return new CreateProductionOrderResult(productionOrder.getId(), orderNumber);
    }

    private void explodeAndCreateItems(ProductionOrder productionOrder, BOM bom, BigDecimal orderedQuantity) {
        var explosionResults = explodeBOMUseCase.execute(new ExplodeBOMUseCase.ExplodeBOMCommand(
                bom.getId(), orderedQuantity, false));

        for (var result : explosionResults) {
            ProductionOrderItem item = ProductionOrderItem.builder()
                    .productionOrder(productionOrder)
                    .componentId(result.componentId())
                    .componentType(result.componentType())
                    .requiredQuantity(result.totalQuantity())
                    .unitOfMeasure(result.unitOfMeasure())
                    .bomItemId(result.bomId())
                    .status(ProductionOrderItem.ItemStatus.PENDING)
                    .build();
            productionOrder.addItem(item);
        }
    }

    private void createOperationsFromRouting(ProductionOrder productionOrder, Routing routing, BigDecimal orderedQuantity) {
        List<RoutingOperation> routingOps = routingOperationRepository.findByRoutingIdOrderBySequence(routing.getId());

        BigDecimal baseQty = routing.getBaseQuantity() != null ? routing.getBaseQuantity() : BigDecimal.ONE;
        BigDecimal multiplier = orderedQuantity.divide(baseQty, 6, RoundingMode.HALF_UP);

        for (RoutingOperation ro : routingOps) {
            ProductionOrderOperation operation = ProductionOrderOperation.builder()
                    .productionOrder(productionOrder)
                    .sequence(ro.getSequence())
                    .routingOperationId(ro.getId())
                    .workCenterId(ro.getWorkCenterId())
                    .workStationId(ro.getWorkStationId())
                    .operationName(ro.getOperationName())
                    .description(ro.getDescription())
                    .plannedSetupTimeMinutes(ro.getSetupTimeMinutes() != null ? ro.getSetupTimeMinutes() : BigDecimal.ZERO)
                    .plannedRunTimeMinutes(ro.getRunTimeMinutes() != null
                            ? ro.getRunTimeMinutes().multiply(multiplier).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO)
                    .crewSize(ro.getCrewSize() != null ? ro.getCrewSize() : 1)
                    .isOutsourced(ro.getIsOutsourced() != null ? ro.getIsOutsourced() : false)
                    .vendorId(ro.getVendorId())
                    .inspectionRequired(ro.getInspectionRequired() != null ? ro.getInspectionRequired() : false)
                    .status(ProductionOrderOperation.OperationStatus.PENDING)
                    .build();
            productionOrder.addOperation(operation);
        }

        productionOrder.setTotalSetupTimeMinutes(routing.getTotalSetupTimeMinutes());
        productionOrder.setTotalRunTimeMinutes(routing.getTotalRunTimeMinutes().multiply(multiplier).setScale(2, RoundingMode.HALF_UP));
    }

    private String generateOrderNumber(OrderType orderType) {
        String prefix = switch (orderType) {
            case MTO -> "MO";
            case MTS -> "MS";
            case ENGINEERING -> "ME";
            case REWORK -> "MR";
            case PROTOTYPE -> "MP";
            case SERVICE -> "SV";
            default -> "PO";
        };
        long count = productionOrderRepository.count() + 1;
        return String.format("%s-%06d", prefix, count);
    }
}