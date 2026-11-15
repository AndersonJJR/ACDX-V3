package com.erp.brazil.manufacturing.application.event;

import com.erp.brazil.manufacturing.domain.event.SalesOrderConfirmedEvent;
import com.erp.brazil.manufacturing.application.usecase.CreateProductionOrderUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class SalesOrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(SalesOrderEventHandler.class);

    private final CreateProductionOrderUseCase createProductionOrderUseCase;

    public SalesOrderEventHandler(CreateProductionOrderUseCase createProductionOrderUseCase) {
        this.createProductionOrderUseCase = createProductionOrderUseCase;
    }

    @EventListener
    public void handleSalesOrderConfirmed(SalesOrderConfirmedEvent event) {
        log.info("Received SalesOrderConfirmedEvent: {}", event.getOrderNumber());

        try {
            createProductionOrderUseCase.createFromSalesOrder(event);
            log.info("Created production orders for sales order: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to create production orders for sales order: {}", event.getOrderNumber(), e);
            throw e;
        }
    }
}