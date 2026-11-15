package com.erp.brazil.fiscal.infrastructure.event;

import com.erp.brazil.fiscal.application.usecase.CalculateTaxesUseCase;
import com.erp.brazil.fiscal.domain.model.TaxCalculationContext;
import com.erp.brazil.fiscal.domain.model.TaxRegime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventConsumer {

    private final CalculateTaxesUseCase calculateTaxesUseCase;

    @RabbitListener(queues = "${fiscal.events.queue.invoice-created:fiscal.invoice.created}")
    public void consumeInvoiceCreated(InvoiceCreatedEvent event) {
        log.info("Received InvoiceCreatedEvent: invoiceId={}, companyCnpj={}", event.invoiceId(), event.companyCnpj());

        try {
            TaxCalculationContext context = buildContext(event);
            calculateTaxesUseCase.execute(context);

            log.info("Taxes calculated for invoice: {}", event.invoiceId());
        } catch (Exception e) {
            log.error("Error calculating taxes for invoice: {}", event.invoiceId(), e);
            // Could send to DLQ or retry
            throw new RuntimeException("Failed to calculate taxes", e);
        }
    }

    private TaxCalculationContext buildContext(InvoiceCreatedEvent event) {
        return TaxCalculationContext.builder()
                .companyId(event.companyId())
                .companyCnpj(event.companyCnpj())
                .companyState(event.companyState())
                .companyMunicipality(event.companyMunicipality())
                .taxRegime(TaxRegime.valueOf(event.taxRegime()))
                .partnerId(event.partnerId())
                .partnerCnpj(event.partnerCnpj())
                .partnerState(event.partnerState())
                .partnerMunicipality(event.partnerMunicipality())
                .partnerIsConsumerFinal(event.partnerIsConsumerFinal())
                .partnerIsForeign(event.partnerIsForeign())
                .operationType(event.operationType())
                .cfopCode(event.cfopCode())
                .ncmCode(event.ncmCode())
                .cestCode(event.cestCode())
                .cnaeCode(event.cnaeCode())
                .serviceCode(event.serviceCode())
                .unitPrice(event.unitPrice())
                .quantity(event.quantity())
                .totalValue(event.totalValue())
                .discountValue(event.discountValue())
                .freightValue(event.freightValue())
                .insuranceValue(event.insuranceValue())
                .otherExpensesValue(event.otherExpensesValue())
                .nfeModel(event.nfeModel())
                .environment(event.environment())
                .build();
    }

    public record InvoiceCreatedEvent(
            String eventId,
            String eventType,
            LocalDateTime timestamp,
            String invoiceId,
            String companyId,
            String companyCnpj,
            String companyState,
            String companyMunicipality,
            String taxRegime,
            String partnerId,
            String partnerCnpj,
            String partnerState,
            String partnerMunicipality,
            boolean partnerIsConsumerFinal,
            boolean partnerIsForeign,
            String operationType,
            String cfopCode,
            String ncmCode,
            String cestCode,
            String cnaeCode,
            String serviceCode,
            BigDecimal unitPrice,
            BigDecimal quantity,
            BigDecimal totalValue,
            BigDecimal discountValue,
            BigDecimal freightValue,
            BigDecimal insuranceValue,
            BigDecimal otherExpensesValue,
            String nfeModel,
            String environment
    ) {}
}