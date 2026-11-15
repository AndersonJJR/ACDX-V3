package com.erp.brazil.manufacturing.infrastructure.messaging;

import com.erp.brazil.manufacturing.application.usecase.CreateProductionOrderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SalesOrderEventConsumer {

    private final CreateProductionOrderUseCase createProductionOrderUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue.sales-order-confirmed:manufacturing.sales-order-confirmed}")
    public void handleSalesOrderConfirmed(SalesOrderConfirmedEvent event) {
        log.info("Received SalesOrderConfirmedEvent: {}", event.getOrderId());

        try {
            CreateProductionOrderUseCase.CreateProductionOrderCommand command = CreateProductionOrderUseCase.CreateProductionOrderCommand.builder()
                    .productId(event.getProductId())
                    .orderedQuantity(event.getQuantity())
                    .unitOfMeasure(event.getUnitOfMeasure())
                    .orderType(CreateProductionOrderUseCase.OrderType.MTO)
                    .priority(CreateProductionOrderUseCase.Priority.NORMAL)
                    .salesOrderId(event.getOrderId())
                    .salesOrderItemId(event.getOrderItemId())
                    .projectId(event.getProjectId())
                    .warehouseId(event.getWarehouseId())
                    .plannedStartDate(event.getRequestedDeliveryDate() != null ?
                            event.getRequestedDeliveryDate().minusDays(7) : LocalDateTime.now().plusDays(7))
                    .plannedEndDate(event.getRequestedDeliveryDate() != null ?
                            event.getRequestedDeliveryDate() : LocalDateTime.now().plusDays(14))
                    .description("Auto-created from Sales Order " + event.getOrderNumber())
                    .notes("Sales Order: " + event.getOrderNumber() + ", Item: " + event.getOrderItemId())
                    .createdBy("system")
                    .build();

            createProductionOrderUseCase.execute(command);
            log.info("Production order created from sales order {}", event.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to create production order from sales order {}", event.getOrderNumber(), e);
            throw new RuntimeException("Failed to process sales order event", e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.sales-order-cancelled:manufacturing.sales-order-cancelled}")
    public void handleSalesOrderCancelled(SalesOrderCancelledEvent event) {
        log.info("Received SalesOrderCancelledEvent: {}", event.getOrderId());
        // TODO: Handle sales order cancellation - cancel related production orders
    }

    public record SalesOrderConfirmedEvent(
            UUID orderId,
            String orderNumber,
            UUID orderItemId,
            UUID productId,
            BigDecimal quantity,
            String unitOfMeasure,
            UUID warehouseId,
            UUID projectId,
            LocalDateTime requestedDeliveryDate
    ) {}

    public record SalesOrderCancelledEvent(
            UUID orderId,
            String orderNumber,
            String reason
    ) {}
}