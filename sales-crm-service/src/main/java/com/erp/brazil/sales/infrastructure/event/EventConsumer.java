package com.erp.brazil.sales.infrastructure.event;

import com.erp.brazil.sales.domain.model.SalesOrder;
import com.erp.brazil.sales.domain.model.SalesOrderStatus;
import com.erp.brazil.sales.domain.model.SalesInvoice;
import com.erp.brazil.sales.domain.model.InvoiceStatus;
import com.erp.brazil.sales.domain.repository.SalesOrderRepository;
import com.erp.brazil.sales.domain.repository.SalesInvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final SalesOrderRepository salesOrderRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;

    public EventConsumer(SalesOrderRepository salesOrderRepository, SalesInvoiceRepository salesInvoiceRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.stock-reserved:sales.stock.reserved}")
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received StockReservedEvent for order: {}", event.orderId());

        SalesOrder order = salesOrderRepository.findById(event.orderId())
                .orElse(null);

        if (order == null) {
            log.warn("Sales order not found: {}", event.orderId());
            return;
        }

        if (event.success()) {
            order.setStatus(SalesOrderStatus.CONFIRMED);
            salesOrderRepository.save(order);
            log.info("Order {} confirmed after stock reservation", event.orderId());
        } else {
            order.setStatus(SalesOrderStatus.ON_HOLD);
            order.setInternalNotes((order.getInternalNotes() != null ? order.getInternalNotes() + "\n" : "")
                    + "Stock reservation failed: " + event.message());
            salesOrderRepository.save(order);
            log.warn("Order {} put on hold due to stock reservation failure: {}", event.orderId(), event.message());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.stock-unreserved:sales.stock.unreserved}")
    @Transactional
    public void handleStockUnreserved(StockUnreservedEvent event) {
        log.info("Received StockUnreservedEvent for order: {}", event.orderId());

        SalesOrder order = salesOrderRepository.findById(event.orderId())
                .orElse(null);

        if (order == null) {
            log.warn("Sales order not found: {}", event.orderId());
            return;
        }

        order.setStatus(SalesOrderStatus.ON_HOLD);
        order.setInternalNotes((order.getInternalNotes() != null ? order.getInternalNotes() + "\n" : "")
                + "Stock unreserved: " + event.reason());
        salesOrderRepository.save(order);
        log.warn("Order {} put on hold due to stock unreservation: {}", event.orderId(), event.reason());
    }

    @RabbitListener(queues = "${rabbitmq.queue.nfe-emitted:sales.nfe.emitted}")
    @Transactional
    public void handleNFEEmitted(NFEEmittedEvent event) {
        log.info("Received NFEEmittedEvent for invoice: {}", event.invoiceId());

        SalesInvoice invoice = salesInvoiceRepository.findById(event.invoiceId())
                .orElse(null);

        if (invoice == null) {
            log.warn("Sales invoice not found: {}", event.invoiceId());
            return;
        }

        invoice.setAccessKey(event.accessKey());
        invoice.setXmlPath(event.xmlPath());
        invoice.setXmlContent(event.xmlContent());
        invoice.setPdfContent(event.pdfContent());
        invoice.setStatus(InvoiceStatus.AUTHORIZED);
        invoice.setAuthorizedAt(event.authorizedAt());
        salesInvoiceRepository.save(invoice);

        log.info("Invoice {} updated with NF-e data: {}", event.invoiceId(), event.accessKey());
    }
}