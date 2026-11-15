package com.erp.brazil.inventory.infrastructure.messaging;

import com.erp.brazil.inventory.application.usecase.ReserveStockUseCase;
import com.erp.brazil.inventory.application.usecase.UnreserveStockUseCase;
import com.erp.brazil.inventory.application.usecase.ReceiveGoodsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final ReserveStockUseCase reserveStockUseCase;
    private final UnreserveStockUseCase unreserveStockUseCase;
    private final ReceiveGoodsUseCase receiveGoodsUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue.sales-order-confirmed:inventory.sales-order-confirmed.queue}")
    public void handleSalesOrderConfirmed(Map<String, Object> event) {
        log.info("Received SalesOrderConfirmedEvent: {}", event);

        UUID companyId = UUID.fromString((String) event.get("companyId"));
        String orderNo = (String) event.get("orderNo");
        UUID orderId = UUID.fromString((String) event.get("orderId"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");

        for (Map<String, Object> item : items) {
            UUID itemId = UUID.fromString((String) item.get("itemId"));
            UUID warehouseId = UUID.fromString((String) item.get("warehouseId"));
            BigDecimal qty = new BigDecimal(item.get("qty").toString());
            UUID itemVariantId = item.get("itemVariantId") != null ? UUID.fromString((String) item.get("itemVariantId")) : null;
            UUID batchId = item.get("batchId") != null ? UUID.fromString((String) item.get("batchId")) : null;
            LocalDateTime expiryDate = item.get("expiryDate") != null ? LocalDateTime.parse((String) item.get("expiryDate")) : null;

            reserveStockUseCase.reserveStock(
                    companyId, itemId, warehouseId, qty,
                    "Sales Order", orderNo, item.get("lineNo").toString(),
                    itemVariantId, batchId, null, null,
                    expiryDate, "Sales Order", orderNo
            );
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.sales-order-cancelled:inventory.sales-order-cancelled.queue}")
    public void handleSalesOrderCancelled(Map<String, Object> event) {
        log.info("Received SalesOrderCancelledEvent: {}", event);

        String orderNo = (String) event.get("orderNo");
        unreserveStockUseCase.unreserveByVoucher("Sales Order", orderNo);
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.production-order-completed:inventory.production-order-completed.queue}")
    public void handleProductionOrderCompleted(Map<String, Object> event) {
        log.info("Received ProductionOrderCompletedEvent: {}", event);

        UUID companyId = UUID.fromString((String) event.get("companyId"));
        String productionOrderNo = (String) event.get("productionOrderNo");
        UUID productionOrderId = UUID.fromString((String) event.get("productionOrderId"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) event.get("items");

        for (Map<String, Object> item : items) {
            UUID itemId = UUID.fromString((String) item.get("itemId"));
            UUID warehouseId = UUID.fromString((String) item.get("warehouseId"));
            BigDecimal qty = new BigDecimal(item.get("qty").toString());
            UUID itemVariantId = item.get("itemVariantId") != null ? UUID.fromString((String) item.get("itemVariantId")) : null;
            UUID batchId = item.get("batchId") != null ? UUID.fromString((String) item.get("batchId")) : null;

            // Create a pseudo-receipt for finished goods
            // This would typically create a Stock Entry directly
            log.info("Finished goods received: item={}, qty={}, warehouse={}", itemId, qty, warehouseId);
        }
    }
}