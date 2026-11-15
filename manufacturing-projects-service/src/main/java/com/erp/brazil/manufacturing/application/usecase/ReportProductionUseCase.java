package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.event.ProductionOrderCompletedEvent;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.WorkOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ReportProductionUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReportProductionUseCase(ProductionOrderRepository productionOrderRepository,
                                    WorkOrderRepository workOrderRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.productionOrderRepository = productionOrderRepository;
        this.workOrderRepository = workOrderRepository;
        this.eventPublisher = eventPublisher;
    }

    public ProductionOrder reportProduction(UUID productionOrderId,
                                             BigDecimal quantityProduced,
                                             BigDecimal quantityScrapped,
                                             UUID warehouseId,
                                             String reportedBy) {
        ProductionOrder order = productionOrderRepository.findById(productionOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Production Order not found: " + productionOrderId));

        if (order.getStatus() != ProductionOrder.ProductionOrderStatus.IN_PROGRESS &&
            order.getStatus() != ProductionOrder.ProductionOrderStatus.RELEASED) {
            throw new IllegalStateException("Cannot report production for order in status: " + order.getStatus());
        }

        order.setProducedQuantity(order.getProducedQuantity().add(quantityProduced));
        order.setScrappedQuantity(order.getScrappedQuantity().add(quantityScrapped));

        // Check if order is complete
        BigDecimal totalProduced = order.getProducedQuantity().add(order.getScrappedQuantity());
        if (totalProduced.compareTo(order.getOrderedQuantity()) >= 0) {
            order.setStatus(ProductionOrder.ProductionOrderStatus.COMPLETED);
            order.setActualEndDate(LocalDateTime.now());
            order.setCompletedDate(LocalDateTime.now());

            // Publish completion event
            eventPublisher.publishEvent(ProductionOrderCompletedEvent.builder()
                .productionOrderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .productId(order.getProductId())
                .producedQuantity(order.getProducedQuantity())
                .scrappedQuantity(order.getScrappedQuantity())
                .warehouseId(warehouseId)
                .completedDate(LocalDateTime.now())
                .completedBy(reportedBy)
                .build());
        }

        return productionOrderRepository.save(order);
    }

    public WorkOrder reportWorkOrderProduction(UUID workOrderId,
                                                BigDecimal quantityProduced,
                                                BigDecimal quantityScrapped,
                                                String reportedBy) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Work Order not found: " + workOrderId));

        if (workOrder.getStatus() != WorkOrder.WorkOrderStatus.IN_PROGRESS &&
            workOrder.getStatus() != WorkOrder.WorkOrderStatus.RELEASED) {
            throw new IllegalStateException("Cannot report production for work order in status: " + workOrder.getStatus());
        }

        workOrder.setCompletedQuantity(workOrder.getCompletedQuantity().add(quantityProduced));
        workOrder.setScrappedQuantity(workOrder.getScrappedQuantity().add(quantityScrapped));

        BigDecimal totalCompleted = workOrder.getCompletedQuantity().add(workOrder.getScrappedQuantity());
        if (totalCompleted.compareTo(workOrder.getPlannedQuantity()) >= 0) {
            workOrder.setStatus(WorkOrder.WorkOrderStatus.COMPLETED);
            workOrder.setActualEndDate(LocalDateTime.now());
        }

        return workOrderRepository.save(workOrder);
    }

    public void reportScrap(UUID productionOrderId, UUID operationId, BigDecimal quantity, String reason, String reportedBy) {
        ProductionOrder order = productionOrderRepository.findById(productionOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Production Order not found: " + productionOrderId));

        order.setScrappedQuantity(order.getScrappedQuantity().add(quantity));

        if (operationId != null) {
            // Would update operation scrap quantity
        }

        productionOrderRepository.save(order);
    }
}