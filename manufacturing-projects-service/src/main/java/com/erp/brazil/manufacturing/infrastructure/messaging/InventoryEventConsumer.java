package com.erp.brazil.manufacturing.infrastructure.messaging;

import com.erp.brazil.manufacturing.application.usecase.ReleaseProductionOrderUseCase;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.ProductionOrderStatus;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final ProductionOrderRepository productionOrderRepository;
    private final ReleaseProductionOrderUseCase releaseProductionOrderUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue.goods-received:manufacturing.goods-received}")
    public void handleGoodsReceived(GoodsReceivedEvent event) {
        log.info("Received GoodsReceivedEvent for production order materials: {}", event.getReferenceId());

        try {
            List<ProductionOrder> orders = productionOrderRepository.findBySalesOrderId(event.getReferenceId());

            for (ProductionOrder order : orders) {
                if (order.getStatus() == ProductionOrderStatus.PLANNED) {
                    log.info("Releasing production order {} due to material availability", order.getOrderNumber());
                    releaseProductionOrderUseCase.execute(new ReleaseProductionOrderUseCase.ReleaseProductionOrderCommand(
                            order.getId(), "system"
                    ));
                }
            }

            // Also check production orders that were waiting for this specific material
            List<ProductionOrder> waitingOrders = productionOrderRepository.findReleasableOrders(
                    List.of(ProductionOrderStatus.PLANNED), java.time.LocalDateTime.now());

            for (ProductionOrder order : waitingOrders) {
                boolean materialsAvailable = checkMaterialsAvailable(order, event);
                if (materialsAvailable) {
                    log.info("Releasing production order {} - materials now available", order.getOrderNumber());
                    releaseProductionOrderUseCase.execute(new ReleaseProductionOrderUseCase.ReleaseProductionOrderCommand(
                            order.getId(), "system"
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Failed to process goods received event for reference {}", event.getReferenceId(), e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.material-reserved:manufacturing.material-reserved}")
    public void handleMaterialReserved(MaterialReservedEvent event) {
        log.info("Received MaterialReservedEvent for production order {}", event.getProductionOrderId());

        productionOrderRepository.findById(event.getProductionOrderId()).ifPresent(order -> {
            if (order.getStatus() == ProductionOrderStatus.PLANNED) {
                boolean allReserved = order.getItems().stream()
                        .allMatch(item -> item.getRequiredQuantity().compareTo(item.getIssuedQuantity()) <= 0);

                if (allReserved) {
                    log.info("All materials reserved for production order {}, releasing", order.getOrderNumber());
                    releaseProductionOrderUseCase.execute(new ReleaseProductionOrderUseCase.ReleaseProductionOrderCommand(
                            order.getId(), "system"
                    ));
                }
            }
        });
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.material-released:manufacturing.material-released}")
    public void handleMaterialReleased(MaterialReleasedEvent event) {
        log.info("Received MaterialReleasedEvent for production order {}", event.getProductionOrderId());
        // Could trigger production order hold if materials were released
    }

    private boolean checkMaterialsAvailable(ProductionOrder order, GoodsReceivedEvent event) {
        return order.getItems().stream()
                .anyMatch(item -> item.getComponentId().equals(event.getProductId()));
    }

    public record GoodsReceivedEvent(
            UUID referenceId,
            String referenceType,
            UUID productId,
            BigDecimal quantity,
            String unitOfMeasure,
            UUID warehouseId
    ) {}

    public record MaterialReservedEvent(
            UUID productionOrderId,
            UUID componentId,
            BigDecimal reservedQuantity
    ) {}

    public record MaterialReleasedEvent(
            UUID productionOrderId,
            UUID componentId,
            BigDecimal releasedQuantity
    ) {}
}