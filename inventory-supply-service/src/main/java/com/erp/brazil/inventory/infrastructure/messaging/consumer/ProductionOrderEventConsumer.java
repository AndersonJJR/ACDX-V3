package com.erp.brazil.inventory.infrastructure.messaging.consumer;

import com.erp.brazil.inventory.application.usecase.ReceiveGoodsUseCase;
import com.erp.brazil.inventory.domain.model.PurchaseReceipt;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptRepository;
import com.erp.brazil.inventory.infrastructure.messaging.event.ProductionOrderCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ProductionOrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductionOrderEventConsumer.class);

    private final ReceiveGoodsUseCase receiveGoodsUseCase;
    private final PurchaseReceiptRepository purchaseReceiptRepository;

    public ProductionOrderEventConsumer(ReceiveGoodsUseCase receiveGoodsUseCase,
                                        PurchaseReceiptRepository purchaseReceiptRepository) {
        this.receiveGoodsUseCase = receiveGoodsUseCase;
        this.purchaseReceiptRepository = purchaseReceiptRepository;
    }

    @RabbitListener(queues = "${app.messaging.queue.production-order-completed:production.order.completed}")
    @Transactional
    public void handleProductionOrderCompleted(ProductionOrderCompletedEvent event) {
        log.info("Received production order completed event: productionOrderId={}, companyId={}, items={}",
                event.productionOrderId(), event.companyId(), event.items().size());

        try {
            // Create a PurchaseReceipt for finished goods receipt
            PurchaseReceipt receipt = createReceiptFromProductionOrder(event);
            PurchaseReceipt savedReceipt = purchaseReceiptRepository.save(receipt);

            // Convert items to receipt items and receive goods
            // This would typically be done via the ReceiveGoodsUseCase
            // For now, we'll log and the actual receipt creation would be handled by a separate process
            log.info("Created receipt {} for production order {}", savedReceipt.getId(), event.productionOrderId());

            // Trigger receive goods
            ReceiveGoodsUseCase.Input input = new ReceiveGoodsUseCase.Input(savedReceipt.getId(), event.items().get(0).itemId()); // userId placeholder
            receiveGoodsUseCase.execute(input);

            log.info("Finished goods received for production order: {}", event.productionOrderId());

        } catch (Exception e) {
            log.error("Error receiving finished goods for production order: {}", event.productionOrderId(), e);
        }
    }

    private PurchaseReceipt createReceiptFromProductionOrder(ProductionOrderCompletedEvent event) {
        // This is a simplified version - in reality, you'd fetch the production order details
        // and create a proper receipt with all items
        PurchaseReceipt receipt = PurchaseReceipt.builder()
                .companyId(event.companyId())
                .supplierId(event.companyId()) // Internal production - company acts as supplier
                .purchaseOrderId(null) // No PO for production
                .receiptNo("PR-PROD-" + event.productionOrderId().toString().substring(0, 8))
                .receiptDate(java.time.LocalDate.now())
                .postingDate(java.time.LocalDate.now())
                .status(PurchaseReceipt.PRStatus.SUBMITTED)
                .currency("BRL")
                .exchangeRate(java.math.BigDecimal.ONE)
                .notes("Finished goods receipt from production order: " + event.productionOrderId())
                .build();

        // Items would be added as PurchaseReceiptItem entities
        return receipt;
    }
}