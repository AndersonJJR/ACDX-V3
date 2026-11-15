package com.erp.brazil.finance.infrastructure.messaging;

import com.erp.brazil.finance.application.event.GoodsReceivedEvent;
import com.erp.brazil.finance.application.event.InvoiceValidatedEvent;
import com.erp.brazil.finance.application.event.PurchaseOrderApprovedEvent;
import com.erp.brazil.finance.domain.model.*;
import com.erp.brazil.finance.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final VendorRepository vendorRepository;
    private final CustomerRepository customerRepository;
    private final PayableInvoiceRepository payableInvoiceRepository;
    private final ReceivableInvoiceRepository receivableInvoiceRepository;
    private final PaymentTermRepository paymentTermRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final GLEntryRepository glEntryRepository;
    private final CostCenterRepository costCenterRepository;
    private final CurrencyRepository currencyRepository;

    public EventConsumer(VendorRepository vendorRepository,
                         CustomerRepository customerRepository,
                         PayableInvoiceRepository payableInvoiceRepository,
                         ReceivableInvoiceRepository receivableInvoiceRepository,
                         PaymentTermRepository paymentTermRepository,
                         ChartOfAccountsRepository chartOfAccountsRepository,
                         FiscalPeriodRepository fiscalPeriodRepository,
                         FiscalYearRepository fiscalYearRepository,
                         JournalEntryRepository journalEntryRepository,
                         JournalEntryLineRepository journalEntryLineRepository,
                         GLEntryRepository glEntryRepository,
                         CostCenterRepository costCenterRepository,
                         CurrencyRepository currencyRepository) {
        this.vendorRepository = vendorRepository;
        this.customerRepository = customerRepository;
        this.payableInvoiceRepository = payableInvoiceRepository;
        this.receivableInvoiceRepository = receivableInvoiceRepository;
        this.paymentTermRepository = paymentTermRepository;
        this.chartOfAccountsRepository = chartOfAccountsRepository;
        this.fiscalPeriodRepository = fiscalPeriodRepository;
        this.fiscalYearRepository = fiscalYearRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.glEntryRepository = glEntryRepository;
        this.costCenterRepository = costCenterRepository;
        this.currencyRepository = currencyRepository;
    }

    @RabbitListener(queues = "fiscal.invoice.validated")
    public void handleInvoiceValidated(InvoiceValidatedEvent event) {
        log.info("Received InvoiceValidatedEvent: {}", event.eventId());

        if (event.type() == com.erp.brazil.finance.domain.model.PaymentType.PAYABLE) {
            createPayableInvoice(event);
        } else {
            createReceivableInvoice(event);
        }
    }

    private void createPayableInvoice(InvoiceValidatedEvent event) {
        Vendor vendor = vendorRepository.findById(event.vendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + event.vendorId()));

        PaymentTerm paymentTerm = null;
        if (event.paymentTermId() != null) {
            paymentTerm = paymentTermRepository.findById(event.paymentTermId()).orElse(null);
        }
        if (paymentTerm == null) {
            List<PaymentTerm> terms = paymentTermRepository.findByCompanyIdAndActiveTrueOrderByCode(event.companyId());
            if (!terms.isEmpty()) {
                paymentTerm = terms.get(0);
            }
        }

        FiscalPeriod fiscalPeriod = findFiscalPeriodForDate(event.companyId(), event.issueDate())
                .orElseThrow(() -> new IllegalStateException("No fiscal period found for date: " + event.issueDate()));

        // Find default currency and payable account
        Currency currency = currencyRepository.findByCode("BRL").orElseThrow();
        ChartOfAccounts payableAccount = chartOfAccountsRepository.findByCode("2.1.1.01").orElseThrow();

        PayableInvoice invoice = new PayableInvoice(
                event.companyId(),
                vendor,
                event.number(),
                event.series(),
                event.issueDate(),
                event.dueDate(),
                fiscalPeriod,
                event.totalAmount(),
                currency,
                payableAccount
        );
        invoice.setTaxAmount(event.taxAmount());
        invoice.setStatus(event.status());
        invoice.setXmlPath(event.xmlPath());
        invoice.setAccessKey(event.accessKey());
        invoice.setPurchaseOrderId(event.purchaseOrderId());
        invoice.setReceiptId(event.receiptId());
        if (paymentTerm != null) {
            invoice.setPaymentTerm(paymentTerm);
        }

        payableInvoiceRepository.save(invoice);
        log.info("Created payable invoice: {}", invoice.getId());
    }

    private void createReceivableInvoice(InvoiceValidatedEvent event) {
        Customer customer = customerRepository.findById(event.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + event.customerId()));

        PaymentTerm paymentTerm = null;
        if (event.paymentTermId() != null) {
            paymentTerm = paymentTermRepository.findById(event.paymentTermId()).orElse(null);
        }
        if (paymentTerm == null && customer.getPaymentTerm() != null) {
            paymentTerm = customer.getPaymentTerm();
        }
        if (paymentTerm == null) {
            List<PaymentTerm> terms = paymentTermRepository.findByCompanyIdAndActiveTrueOrderByCode(event.companyId());
            if (!terms.isEmpty()) {
                paymentTerm = terms.get(0);
            }
        }

        FiscalPeriod fiscalPeriod = findFiscalPeriodForDate(event.companyId(), event.issueDate())
                .orElseThrow(() -> new IllegalStateException("No fiscal period found for date: " + event.issueDate()));

        // Find default currency and receivable account
        Currency currency = currencyRepository.findByCode("BRL").orElseThrow();
        ChartOfAccounts receivableAccount = chartOfAccountsRepository.findByCode("1.1.2.01").orElseThrow();

        ReceivableInvoice invoice = new ReceivableInvoice(
                event.companyId(),
                customer,
                event.number(),
                event.series(),
                event.issueDate(),
                event.dueDate(),
                fiscalPeriod,
                event.totalAmount(),
                currency,
                receivableAccount
        );
        invoice.setTaxAmount(event.taxAmount());
        invoice.setStatus(event.status());
        invoice.setXmlPath(event.xmlPath());
        invoice.setAccessKey(event.accessKey());
        invoice.setSalesOrderId(event.salesOrderId());
        if (paymentTerm != null) {
            invoice.setPaymentTerm(paymentTerm);
        }

        receivableInvoiceRepository.save(invoice);
        log.info("Created receivable invoice: {}", invoice.getId());
    }

    private Optional<FiscalPeriod> findFiscalPeriodForDate(UUID companyId, LocalDate date) {
        // Find fiscal year for the company that contains this date
        // This is a simplified implementation - in reality you'd have a company-specific fiscal year
        List<FiscalPeriod> periods = fiscalPeriodRepository.findAll();
        return periods.stream()
                .filter(fp -> !fp.getFiscalYear().isClosed())
                .filter(fp -> date.compareTo(fp.getStartDate()) >= 0 && date.compareTo(fp.getEndDate()) <= 0)
                .findFirst();
    }

    @RabbitListener(queues = "inventory.goods.received")
    public void handleGoodsReceived(GoodsReceivedEvent event) {
        log.info("Received GoodsReceivedEvent: {}", event.eventId());
        // Three-way matching logic would go here
        // Match Purchase Order -> Goods Receipt -> Invoice
    }

    @RabbitListener(queues = "purchase.order.approved")
    public void handlePurchaseOrderApproved(PurchaseOrderApprovedEvent event) {
        log.info("Received PurchaseOrderApprovedEvent: {}", event.eventId());
        // Budget reservation logic would go here
    }
}