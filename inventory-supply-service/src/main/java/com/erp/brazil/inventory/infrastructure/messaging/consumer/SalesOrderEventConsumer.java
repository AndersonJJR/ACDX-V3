package com.erp.brazil.inventory.infrastructure.messaging.consumer;

import com.erp.brazil.inventory.application.usecase.ReserveStockUseCase;
import com.erp.brazil.inventory.application.usecase.UnreserveStockUseCase;
import com.erp.brazil.inventory.infrastructure.messaging.event.SalesOrderCancelledEvent;
import com.erp.brazil.inventory.infrastructure.messaging.event.SalesOrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

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
                event.getSalesOrderId(), event.getCompanyId(), event.getItems().size());

        try {
            List<ReserveStockUseCase.Input.ReservationItem> reservationItems = event.getItems().stream()
                    .map(item -> new ReserveStockUseCase.Input.ReservationItem(
                            item.getItemId(),
                            item.getWarehouseId(),
                            item.getQty(),
                            item.getItemVariantId()
                    ))
                    .toList();

            ReserveStockUseCase.Input input = new ReserveStockUseCase.Input(event.getSalesOrderId(), reservationItems);
            reserveStockUseCase.execute(input);

            log.info("Stock reserved successfully for sales order: {}", event.getSalesOrderId());

        } catch (ReserveStockUseCase.InsufficientStockException e) {
            log.warn("Insufficient stock for sales order {}: {}", event.getSalesOrderId(), e.getMessage());
            // Publish compensation event or handle accordingly
        } catch (Exception e) {
            log.error("Error reserving stock for sales order: {}", event.getSalesOrderId(), e);
        }
    }

    @RabbitListener(queues = "${app.messaging.queue.sales-order-cancelled:sales.order.cancelled}")
    public void handleSalesOrderCancelled(SalesOrderCancelledEvent event) {
        log.info("Received sales order cancelled event: salesOrderId={}, reason={}", event.getSalesOrderId(), event.getReason());

        try {
            UnreserveStockUseCase.Input input = new UnreserveStockUseCase.Input(event.getSalesOrderId(), event.getReason());
            unreserveStockUseCase.execute(input);

            log.info("Stock unreserved successfully for sales order: {}", event.getSalesOrderId());

        } catch (Exception e) {
            log.error("Error unreserving stock for sales order: {}", event.getSalesOrderId(), e);
        }
    }
}