package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.event.SubcontractingOrderSentEvent;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrderItem;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderOperationRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.SubcontractingOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubcontractOutUseCase {

    private final SubcontractingOrderRepository subcontractingOrderRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderOperationRepository operationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SubcontractOutUseCase(SubcontractingOrderRepository subcontractingOrderRepository,
                                  ProductionOrderRepository productionOrderRepository,
                                  ProductionOrderOperationRepository operationRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.subcontractingOrderRepository = subcontractingOrderRepository;
        this.productionOrderRepository = productionOrderRepository;
        this.operationRepository = operationRepository;
        this.eventPublisher = eventPublisher;
    }

    public SubcontractingOrder createSubcontractingOrder(UUID productionOrderId,
                                                          UUID productionOrderOperationId,
                                                          UUID vendorId,
                                                          UUID productId,
                                                          BigDecimal quantity,
                                                          String unitOfMeasure,
                                                          BigDecimal unitPrice,
                                                          LocalDateTime plannedEndDate,
                                                          UUID warehouseId,
                                                          String createdBy) {
        ProductionOrder order = productionOrderRepository.findById(productionOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Production Order not found: " + productionOrderId));

        ProductionOrderOperation operation = operationRepository.findById(productionOrderOperationId)
            .orElseThrow(() -> new IllegalArgumentException("Production Order Operation not found: " + productionOrderOperationId));

        String orderNumber = generateOrderNumber();

        SubcontractingOrder subOrder = SubcontractingOrder.builder()
            .orderNumber(orderNumber)
            .vendorId(vendorId)
            .productionOrderId(productionOrderId)
            .productionOrderOperationId(productionOrderOperationId)
            .productId(productId)
            .status(SubcontractingOrder.SubcontractingStatus.DRAFT)
            .description("Subcontracting for " + order.getOrderNumber() + " - " + operation.getOperationName())
            .orderedQuantity(quantity)
            .unitOfMeasure(unitOfMeasure)
            .unitPrice(unitPrice)
            .totalAmount(unitPrice.multiply(quantity))
            .currency("BRL")
            .plannedStartDate(LocalDateTime.now().plusDays(1))
            .plannedEndDate(plannedEndDate)
            .warehouseId(warehouseId)
            .build();

        // Add items (raw materials to send)
        // This would typically come from the BOM explosion for the operation
        // For now, we'll add a placeholder
        SubcontractingOrderItem item = SubcontractingOrderItem.builder()
            .componentId(productId) // Would be raw material
            .quantity(quantity)
            .unitOfMeasure(unitOfMeasure)
            .warehouseId(warehouseId)
            .build();
        subOrder.addItem(item);

        SubcontractingOrder saved = subcontractingOrderRepository.save(subOrder);

        // Update production order operation
        operation.setIsOutsourced(true);
        operation.setVendorId(vendorId);
        operation.setSubcontractingOrderId(saved.getId());
        operation.setStatus(ProductionOrderOperation.OperationStatus.WAITING_CAPACITY); // Waiting for vendor
        operationRepository.save(operation);

        return saved;
    }

    public SubcontractingOrder sendSubcontractingOrder(UUID subcontractingOrderId, String sentBy) {
        SubcontractingOrder order = subcontractingOrderRepository.findById(subcontractingOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Subcontracting Order not found: " + subcontractingOrderId));

        if (order.getStatus() != SubcontractingOrder.SubcontractingStatus.DRAFT) {
            throw new IllegalStateException("Order cannot be sent. Current status: " + order.getStatus());
        }

        order.setStatus(SubcontractingOrder.SubcontractingStatus.SENT);
        order.setActualStartDate(LocalDateTime.now());

        SubcontractingOrder saved = subcontractingOrderRepository.save(order);

        // Publish event
        eventPublisher.publishEvent(SubcontractingOrderSentEvent.builder()
            .subcontractingOrderId(saved.getId())
            .orderNumber(saved.getOrderNumber())
            .vendorId(saved.getVendorId())
            .productId(saved.getProductId())
            .orderedQuantity(saved.getOrderedQuantity())
            .warehouseId(saved.getWarehouseId())
            .plannedEndDate(saved.getPlannedEndDate())
            .sentBy(sentBy)
            .build());

        return saved;
    }

    public void sendMaterials(UUID subcontractingOrderId, List<MaterialShipment> shipments, String shippedBy) {
        SubcontractingOrder order = subcontractingOrderRepository.findById(subcontractingOrderId)
            .orElseThrow(() -> new IllegalArgumentException("Subcontracting Order not found: " + subcontractingOrderId));

        for (MaterialShipment shipment : shipments) {
            SubcontractingOrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(shipment.itemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + shipment.itemId()));

            item.setSentQuantity(item.getSentQuantity().add(shipment.quantity()));
        }

        order.setStatus(SubcontractingOrder.SubcontractingStatus.IN_PROGRESS);
        subcontractingOrderRepository.save(order);
    }

    private String generateOrderNumber() {
        long count = subcontractingOrderRepository.count() + 1;
        return String.format("SC-%06d", count);
    }

    public record MaterialShipment(UUID itemId, BigDecimal quantity) {}
}