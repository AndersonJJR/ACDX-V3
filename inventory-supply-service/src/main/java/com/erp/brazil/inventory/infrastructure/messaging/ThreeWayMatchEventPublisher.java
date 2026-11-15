package com.erp.brazil.inventory.infrastructure.messaging;

import com.erp.brazil.inventory.domain.model.PurchaseInvoice;
import com.erp.brazil.inventory.domain.model.PurchaseInvoiceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThreeWayMatchEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String exchange;

    @Value("${app.messaging.routing-key.invoice-matched:invoice.matched}")
    private String matchedRoutingKey;

    @Value("${app.messaging.routing-key.invoice-mismatch:invoice.mismatch}")
    private String mismatchRoutingKey;

    public void publishMatched(PurchaseInvoice invoice) {
        Map<String, Object> event = Map.of(
                "eventType", "PurchaseInvoiceMatchedEvent",
                "invoiceId", invoice.getId(),
                "invoiceNo", invoice.getInvoiceNo(),
                "companyId", invoice.getCompanyId(),
                "supplierId", invoice.getSupplier().getId(),
                "grandTotal", invoice.getGrandTotal(),
                "currency", invoice.getCurrency(),
                "timestamp", java.time.Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, matchedRoutingKey, event);
        log.info("Published PurchaseInvoiceMatchedEvent for invoice: {}", invoice.getInvoiceNo());
    }

    public void publishMismatch(PurchaseInvoice invoice, List<PurchaseInvoiceItem> mismatchedItems) {
        List<Map<String, Object>> itemMismatches = mismatchedItems.stream()
                .map(item -> Map.of(
                        "itemId", item.getItem().getId(),
                        "matchStatus", item.getMatchStatus().name(),
                        "poQty", item.getPoQty(),
                        "receivedQty", item.getReceivedQty(),
                        "billedQty", item.getQty(),
                        "varianceQty", item.getVarianceQty(),
                        "varianceAmount", item.getVarianceAmount()
                ))
                .collect(Collectors.toList());

        Map<String, Object> event = Map.of(
                "eventType", "PurchaseInvoiceMismatchEvent",
                "invoiceId", invoice.getId(),
                "invoiceNo", invoice.getInvoiceNo(),
                "companyId", invoice.getCompanyId(),
                "supplierId", invoice.getSupplier().getId(),
                "itemMismatches", itemMismatches,
                "timestamp", java.time.Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, mismatchRoutingKey, event);
        log.warn("Published PurchaseInvoiceMismatchEvent for invoice: {}", invoice.getInvoiceNo());
    }

    public void publishStockReserved(UUID companyId, UUID itemId, UUID warehouseId, BigDecimal qty, String voucherType, String voucherNo) {
        Map<String, Object> event = Map.of(
                "eventType", "StockReservedEvent",
                "companyId", companyId,
                "itemId", itemId,
                "warehouseId", warehouseId,
                "qty", qty,
                "voucherType", voucherType,
                "voucherNo", voucherNo,
                "timestamp", java.time.Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, "stock.reserved", event);
    }

    public void publishStockUnreserved(UUID companyId, UUID itemId, UUID warehouseId, BigDecimal qty, String voucherType, String voucherNo) {
        Map<String, Object> event = Map.of(
                "eventType", "StockUnreservedEvent",
                "companyId", companyId,
                "itemId", itemId,
                "warehouseId", warehouseId,
                "qty", qty,
                "voucherType", voucherType,
                "voucherNo", voucherNo,
                "timestamp", java.time.Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, "stock.unreserved", event);
    }

    public void publishGoodsReceived(UUID companyId, UUID receiptId, String receiptNo) {
        Map<String, Object> event = Map.of(
                "eventType", "GoodsReceivedEvent",
                "companyId", companyId,
                "receiptId", receiptId,
                "receiptNo", receiptNo,
                "timestamp", java.time.Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, "goods.received", event);
    }

    public void publishValuationRecalculated(UUID companyId, UUID jobId, long processedRecords, long failedRecords) {
        Map<String, Object> event = Map.of(
                "eventType", "ValuationRecalculatedEvent",
                "companyId", companyId,
                "jobId", jobId,
                "processedRecords", processedRecords,
                "failedRecords", failedRecords,
                "timestamp", java.time.Instant.now().toString()
        );

        rabbitTemplate.convertAndSend(exchange, "valuation.recalculated", event);
    }
}