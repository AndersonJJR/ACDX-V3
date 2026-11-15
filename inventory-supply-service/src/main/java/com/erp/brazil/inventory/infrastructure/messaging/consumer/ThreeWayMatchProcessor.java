package com.erp.brazil.inventory.infrastructure.messaging.consumer;

import com.erp.brazil.inventory.application.usecase.ProcessPurchaseInvoiceUseCase;
import com.erp.brazil.inventory.infrastructure.messaging.event.PurchaseInvoiceCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ThreeWayMatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThreeWayMatchProcessor.class);

    private final ProcessPurchaseInvoiceUseCase processPurchaseInvoiceUseCase;

    public ThreeWayMatchProcessor(ProcessPurchaseInvoiceUseCase processPurchaseInvoiceUseCase) {
        this.processPurchaseInvoiceUseCase = processPurchaseInvoiceUseCase;
    }

    @RabbitListener(queues = "${app.messaging.queue.three-way-match:finance.purchase_invoice.created}")
    public void handlePurchaseInvoiceCreated(PurchaseInvoiceCreatedEvent event) {
        log.info("Received purchase invoice created event: invoiceId={}, companyId={}", event.getInvoiceId(), event.getCompanyId());

        try {
            ProcessPurchaseInvoiceUseCase.Input input = new ProcessPurchaseInvoiceUseCase.Input(event.getInvoiceId());
            ProcessPurchaseInvoiceUseCase.Output output = processPurchaseInvoiceUseCase.execute(input);

            log.info("3-way match completed for invoice: {}. Matched: {}, Mismatches: {}",
                    event.getInvoiceId(), output.fullyMatched(), output.mismatches().size());

        } catch (Exception e) {
            log.error("Error processing 3-way match for invoice: {}", event.getInvoiceId(), e);
        }
    }
}