package com.erp.brazil.inventory.infrastructure.messaging.consumer;

import com.erp.brazil.inventory.application.usecase.ReserveStockUseCase;
import com.erp.brazil.inventory.application.usecase.UnreserveStockUseCase;
import com.erp.brazil.inventory.infrastructure.messaging.event.SalesOrderCancelledEvent;
import com.erp.brazil.inventory.infrastructure.messaging.event.SalesOrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SalesOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SalesOrderEventConsumer.class);

    private final ReserveStockUseCase reserveStockUseCase;
    private final UnreserveStockUseCase unreserveStockUseCase;

    public SalesOrderEventConsumer(ReserveStockUseCase reserveStockUseCase,
                                   UnreserveStockUseCase unreserveStockUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.unreserveStockUseCase = unreserveStockUseCase;
    }

    @RabbitListener(queues = "${app.messaging.queue.sales-order-confirmed:sales.order.confirmed}")
    public void handleSalesOrderConfirmed(SalesOrderConfirmedEvent event) {
        log.info("Received sales order confirmed event: salesOrderId={}, companyId={}, items={}",
                event.salesOrderId(), event.companyId(), event.items().size());

        try {
            List<ReserveStockUseCase.Input.ReservationItem> reservationItems = event.items().stream()
                    .map(item -> new ReserveStockUseCase.Input.ReservationItem(
                            item.itemId(),
                            item.warehouseId(),
                            item.qty(),
                            item.itemVariantId()
                    ))
                    .toList();

            ReserveStockUseCase.Input input = new ReserveStockUseCase.Input(event.salesOrderId(), reservationItems);
            reserveStockUseCase.execute(input);

            log.info("Stock reserved successfully for sales order: {}", event.salesOrderId());

        } catch (ReserveStockUseCase.InsufficientStockException e) {
            log.warn("Insufficient stock for sales order {}: {}", event.salesOrderId(), e.getMessage());
            // Publish compensation event or handle accordingly
        } catch (Exception e) {
            log.error("Error reserving stock for sales order: {}", event.salesOrderId(), e);
        }
    }

    @RabbitListener(queues = "${app.messaging.queue.sales-order-cancelled:sales.order.cancelled}")
    public void handleSalesOrderCancelled(SalesOrderCancelledEvent event) {
        log.info("Received sales order cancelled event: salesOrderId={}, reason={}", event.salesOrderId(), event.reason());

        try {
            UnreserveStockUseCase.Input input = new UnreserveStockUseCase.Input(event.salesOrderId(), event.reason());
            unreserveStockUseCase.execute(input);

            log.info("Stock unreserved successfully for sales order: {}", event.salesOrderId());

        } catch (Exception e) {
            log.error("Error unreserving stock for sales order: {}", event.salesOrderId(), e);
        }
    }
}