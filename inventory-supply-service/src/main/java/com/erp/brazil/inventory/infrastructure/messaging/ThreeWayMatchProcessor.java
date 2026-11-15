package com.erp.brazil.inventory.infrastructure.messaging;

import com.erp.brazil.inventory.application.usecase.ProcessPurchaseInvoiceUseCase;
import com.erp.brazil.inventory.domain.model.PurchaseInvoice;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThreeWayMatchProcessor {

    private final PurchaseInvoiceRepository invoiceRepository;
    private final ProcessPurchaseInvoiceUseCase processPurchaseInvoiceUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue.three-way-match:inventory.three-way-match.queue}")
    public void processThreeWayMatch(Map<String, Object> message) {
        Object invoiceIdObj = message.get("invoiceId");
        if (invoiceIdObj == null) {
            log.warn("Received 3-way match message without invoiceId");
            return;
        }

        UUID invoiceId = UUID.fromString(invoiceIdObj.toString());
        log.info("Processing 3-way match for invoice: {}", invoiceId);

        try {
            PurchaseInvoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

            processPurchaseInvoiceUseCase.processInvoice(invoice.getCompanyId(), invoiceId);
        } catch (Exception e) {
            log.error("Failed to process 3-way match for invoice: {}", invoiceId, e);
            throw e; // Re-throw for retry
        }
    }
}