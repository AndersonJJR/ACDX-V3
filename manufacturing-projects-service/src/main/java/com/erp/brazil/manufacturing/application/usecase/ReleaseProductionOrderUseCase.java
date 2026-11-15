package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.event.ProductionOrderReleasedEvent;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ReleaseProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReleaseProductionOrderUseCase(ProductionOrderRepository productionOrderRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.productionOrderRepository = productionOrderRepository;
        this.eventPublisher = eventPublisher;
    }

    public ProductionOrder release(UUID productionOrderId, String releasedBy) {
        ProductionOrder order = productionOrderRepository.findById(productionOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Production Order not found: " + productionOrderId));

        if (!order.canBeReleased()) {
            throw new IllegalStateException("Production Order cannot be released. Current status: " + order.getStatus());
        }

        // Validate material availability
        validateMaterialAvailability(order);

        // Validate capacity availability
        validateCapacityAvailability(order);

        order.setStatus(ProductionOrder.ProductionOrderStatus.RELEASED);
        order.setReleasedDate(LocalDateTime.now());
        order.setUpdatedBy(releasedBy);

        ProductionOrder saved = productionOrderRepository.save(order);

        // Publish event
        eventPublisher.publishEvent(ProductionOrderReleasedEvent.builder()
            .productionOrderId(saved.getId())
            .orderNumber(saved.getOrderNumber())
            .productId(saved.getProductId())
            .orderedQuantity(saved.getOrderedQuantity())
            .warehouseId(saved.getWarehouseId())
            .plannedStartDate(saved.getPlannedStartDate())
            .plannedEndDate(saved.getPlannedEndDate())
            .releasedBy(releasedBy)
            .build());

        return saved;
    }

    public void releaseBatch(List<UUID> productionOrderIds, String releasedBy) {
        for (UUID id : productionOrderIds) {
            try {
                release(id, releasedBy);
            } catch (Exception e) {
                // Log error but continue with others
                System.err.println("Failed to release production order " + id + ": " + e.getMessage());
            }
        }
    }

    private void validateMaterialAvailability(ProductionOrder order) {
        for (var item : order.getItems()) {
            if (item.getRequiredQuantity().compareTo(item.getIssuedQuantity()) > 0) {
                // Material not fully available - could warn or block based on configuration
                // For now, just log warning
                System.out.println("Warning: Material not fully available for item " + item.getComponentId());
            }
        }
    }

    private void validateCapacityAvailability(ProductionOrder order) {
        // Check if work centers have capacity for planned dates
        for (var operation : order.getOperations()) {
            if (operation.getWorkCenterId() != null) {
                // Would check work center capacity here
                // For now, just log
                System.out.println("Checking capacity for work center " + operation.getWorkCenterId());
            }
        }
    }
}