package com.erp.brazil.manufacturing.application.event;

import com.erp.brazil.manufacturing.domain.event.GoodsReceivedEvent;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class GoodsReceivedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(GoodsReceivedEventHandler.class);

    private final ProductionOrderRepository productionOrderRepository;

    public GoodsReceivedEventHandler(ProductionOrderRepository productionOrderRepository) {
        this.productionOrderRepository = productionOrderRepository;
    }

    @EventListener
    public void handleGoodsReceived(GoodsReceivedEvent event) {
        log.info("Received GoodsReceivedEvent: {} for product {}", event.getReceiptNumber(), event.getProductId());

        // Find production orders waiting for this material
        List<ProductionOrder> orders = productionOrderRepository.findByProductId(event.getProductId());

        for (ProductionOrder order : orders) {
            if (order.getStatus() == ProductionOrder.ProductionOrderStatus.RELEASED ||
                order.getStatus() == ProductionOrder.ProductionOrderStatus.PLANNED) {

                // Check if order has this material as a component
                for (ProductionOrderItem item : order.getItems()) {
                    if (item.getComponentId().equals(event.getProductId()) &&
                        item.getWarehouseId().equals(event.getWarehouseId())) {

                        // Update issued quantity
                        item.setIssuedQuantity(item.getIssuedQuantity().add(event.getReceivedQuantity()));

                        if (item.isFullyIssued()) {
                            item.setStatus(ProductionOrderItem.ItemStatus.ISSUED);
                        } else {
                            item.setStatus(ProductionOrderItem.ItemStatus.PARTIAL);
                        }
                    }
                }

                // Check if all materials are available to release
                boolean allMaterialsAvailable = order.getItems().stream()
                    .allMatch(i -> i.getStatus() == ProductionOrderItem.ItemStatus.ISSUED ||
                                  i.getStatus() == ProductionOrderItem.ItemStatus.CONSUMED);

                if (allMaterialsAvailable && order.getStatus() == ProductionOrder.ProductionOrderStatus.PLANNED) {
                    // Could auto-release here or notify
                    log.info("All materials available for production order: {}", order.getOrderNumber());
                }

                productionOrderRepository.save(order);
            }
        }
    }
}