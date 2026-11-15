package com.erp.brazil.fiscal.infrastructure.messaging;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.application.usecase.CalculateTaxesUseCase;
import com.erp.brazil.fiscal.domain.model.TaxCalculationContext;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Event consumer for domain events.
 * Consumes InvoiceCreatedEvent from billing service and triggers tax calculation.
 */
@Component
public class EventConsumer {

    private final CalculateTaxesUseCase calculateTaxesUseCase;

    public EventConsumer(CalculateTaxesUseCase calculateTaxesUseCase) {
        this.calculateTaxesUseCase = calculateTaxesUseCase;
    }

    @RabbitListener(queues = "${messaging.queue.invoice-created:fiscal.invoice.created}")
    public void consumeInvoiceCreated(EventDTO.InvoiceCreatedEvent event) {
        // Convert invoice event to tax calculation context
        TaxCalculationContext context = new TaxCalculationContext(
                event.companyId(),
                event.originState(),
                event.destinationState(),
                event.ncm(),
                event.cest(),
                event.cfop(),
                event.totalValue(),
                event.items().isEmpty() ? BigDecimal.ONE : event.items().get(0).quantity(),
                TaxCalculationContext.OperationType.SALE,
                event.taxRegime(),
                null, // municipalityCode
                null  // cnaeCode
        );

        // Calculate taxes
        calculateTaxesUseCase.execute(context);
    }

    @RabbitListener(queues = "${messaging.queue.nfe-cancel-request:fiscal.nfe.cancel.request}")
    public void consumeNfeCancelRequest(EventDTO.NFECancelRequest event) {
        // Handle NF-e cancel request from external system
        // This would trigger CancelNFEUseCase
    }

    @RabbitListener(queues = "${messaging.queue.sped-generate-request:fiscal.sped.generate.request}")
    public void consumeSpedGenerateRequest(EventDTO.SPEDGenerateRequest event) {
        // Handle SPED generation request
        // This would trigger GenerateSPEDUseCase
    }
}