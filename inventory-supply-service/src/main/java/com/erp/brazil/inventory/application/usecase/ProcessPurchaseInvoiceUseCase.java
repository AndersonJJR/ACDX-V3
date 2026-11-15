package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.PurchaseInvoice;
import com.erp.brazil.inventory.domain.model.PurchaseInvoiceItem;
import com.erp.brazil.inventory.domain.model.PurchaseOrderItem;
import com.erp.brazil.inventory.domain.model.PurchaseReceiptItem;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceItemRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseInvoiceRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseOrderItemRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptItemRepository;
import com.erp.brazil.inventory.infrastructure.messaging.event.PurchaseInvoiceMatchedEvent;
import com.erp.brazil.inventory.infrastructure.messaging.event.PurchaseInvoiceMismatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProcessPurchaseInvoiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPurchaseInvoiceUseCase.class);

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final PurchaseInvoiceItemRepository purchaseInvoiceItemRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseReceiptItemRepository purchaseReceiptItemRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.invoice-matched:invoice.matched}")
    private String invoiceMatchedRoutingKey;

    @Value("${app.messaging.routing-key.invoice-mismatch:invoice.mismatch}")
    private String invoiceMismatchRoutingKey;

    @Value("${app.three-way-match.qty-tolerance:0.01}")
    private BigDecimal qtyTolerance;

    @Value("${app.three-way-match.rate-tolerance:0.01}")
    private BigDecimal rateTolerance;

    public ProcessPurchaseInvoiceUseCase(PurchaseInvoiceRepository purchaseInvoiceRepository,
                                         PurchaseInvoiceItemRepository purchaseInvoiceItemRepository,
                                         PurchaseOrderItemRepository purchaseOrderItemRepository,
                                         PurchaseReceiptItemRepository purchaseReceiptItemRepository,
                                         RabbitTemplate rabbitTemplate) {
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.purchaseInvoiceItemRepository = purchaseInvoiceItemRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.purchaseReceiptItemRepository = purchaseReceiptItemRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Input(UUID invoiceId) {}

    public record Output(PurchaseInvoice invoice, boolean fullyMatched, List<MismatchDetail> mismatches) {
        public record MismatchDetail(UUID invoiceItemId, String type, BigDecimal expected, BigDecimal actual, BigDecimal variance) {}
    }

    public Output execute(Input input) {
        log.info("Processing 3-way match for purchase invoice: {}", input.invoiceId());

        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(input.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase invoice not found: " + input.invoiceId()));

        if (invoice.getStatus() != PurchaseInvoice.PIStatus.VALIDATED &&
            invoice.getStatus() != PurchaseInvoice.PIStatus.APPROVED) {
            throw new IllegalStateException("Invoice must be in VALIDATED or APPROVED status for matching. Current: " + invoice.getStatus());
        }

        List<PurchaseInvoiceItem> invoiceItems = purchaseInvoiceItemRepository.findByPurchaseInvoiceId(input.invoiceId());
        List<Output.MismatchDetail> allMismatches = new java.util.ArrayList<>();
        boolean fullyMatched = true;

        for (PurchaseInvoiceItem invoiceItem : invoiceItems) {
            MatchResult result = performThreeWayMatch(invoice, invoiceItem);
            invoiceItem.setMatchStatus(result.matchStatus());
            invoiceItem.setVarianceQty(result.qtyVariance());
            invoiceItem.setVarianceAmount(result.amountVariance());
            purchaseInvoiceItemRepository.save(invoiceItem);

            if (result.matchStatus() != PurchaseInvoiceItem.MatchStatus.MATCHED) {
                fullyMatched = false;
                allMismatches.addAll(result.mismatches());
            }
        }

        // Update invoice status based on match results
        if (fullyMatched) {
            invoice.setStatus(PurchaseInvoice.PIStatus.APPROVED);
            invoice.setIsBlockedForPayment(false);
            invoice.setBlockReason(null);
        } else {
            invoice.setStatus(PurchaseInvoice.PIStatus.BLOCKED);
            invoice.setIsBlockedForPayment(true);
            invoice.setBlockReason("3-way match failed: " + allMismatches.size() + " mismatches found");
        }

        purchaseInvoiceRepository.save(invoice);

        // Publish events
        if (fullyMatched) {
            publishMatchedEvent(invoice);
        } else {
            publishMismatchEvent(invoice, allMismatches);
        }

        log.info("3-way match completed for invoice: {}. Fully matched: {}, Mismatches: {}", input.invoiceId(), fullyMatched, allMismatches.size());
        return new Output(invoice, fullyMatched, allMismatches);
    }

    private MatchResult performThreeWayMatch(PurchaseInvoice invoice, PurchaseInvoiceItem invoiceItem) {
        PurchaseInvoiceItem.MatchStatus overallStatus = PurchaseInvoiceItem.MatchStatus.MATCHED;
        List<Output.MismatchDetail> mismatches = new java.util.ArrayList<>();
        BigDecimal totalQtyVariance = BigDecimal.ZERO;
        BigDecimal totalAmountVariance = BigDecimal.ZERO;

        // 1. Quantity Match: PO Qty vs Receipt Qty vs Invoice Qty
        BigDecimal poQty = getPoQty(invoiceItem);
        BigDecimal receiptQty = getReceiptQty(invoiceItem);
        BigDecimal invoiceQty = invoiceItem.getQty();

        if (poQty != null && receiptQty != null) {
            // Check PO vs Receipt
            BigDecimal poReceiptVariance = poQty.subtract(receiptQty).abs();
            if (poReceiptVariance.compareTo(qtyTolerance) > 0) {
                overallStatus = determineMismatchStatus(overallStatus, true, false);
                mismatches.add(new Output.MismatchDetail(
                        invoiceItem.getId(), "PO_RECEIPT_QTY_MISMATCH", poQty, receiptQty, poReceiptVariance));
            }

            // Check Receipt vs Invoice
            BigDecimal receiptInvoiceVariance = receiptQty.subtract(invoiceQty).abs();
            if (receiptInvoiceVariance.compareTo(qtyTolerance) > 0) {
                overallStatus = determineMismatchStatus(overallStatus, true, false);
                mismatches.add(new Output.MismatchDetail(
                        invoiceItem.getId(), "RECEIPT_INVOICE_QTY_MISMATCH", receiptQty, invoiceQty, receiptInvoiceVariance));
                totalQtyVariance = totalQtyVariance.add(receiptInvoiceVariance);
            }

            // Check PO vs Invoice (if no receipt or partial receipt)
            if (receiptQty.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal poInvoiceVariance = poQty.subtract(invoiceQty).abs();
                if (poInvoiceVariance.compareTo(qtyTolerance) > 0) {
                    overallStatus = determineMismatchStatus(overallStatus, true, false);
                    mismatches.add(new Output.MismatchDetail(
                            invoiceItem.getId(), "PO_INVOICE_QTY_MISMATCH", poQty, invoiceQty, poInvoiceVariance));
                    totalQtyVariance = totalQtyVariance.add(poInvoiceVariance);
                }
            }
        }

        // 2. Rate Match: PO Rate vs Invoice Rate
        BigDecimal poRate = getPoRate(invoiceItem);
        BigDecimal invoiceRate = invoiceItem.getRate();

        if (poRate != null && invoiceRate != null) {
            BigDecimal rateVariance = poRate.subtract(invoiceRate).abs();
            if (rateVariance.compareTo(rateTolerance) > 0) {
                overallStatus = determineMismatchStatus(overallStatus, false, true);
                mismatches.add(new Output.MismatchDetail(
                        invoiceItem.getId(), "RATE_MISMATCH", poRate, invoiceRate, rateVariance));
                totalAmountVariance = totalAmountVariance.add(rateVariance.multiply(invoiceQty));
            }
        }

        // 3. Over-billing check: Invoice Qty > Receipt Qty
        if (receiptQty != null && invoiceQty.compareTo(receiptQty) > 0) {
            BigDecimal overBilledQty = invoiceQty.subtract(receiptQty);
            overallStatus = PurchaseInvoiceItem.MatchStatus.OVER_BILLED;
            mismatches.add(new Output.MismatchDetail(
                    invoiceItem.getId(), "OVER_BILLED", receiptQty, invoiceQty, overBilledQty));
        }

        // Set PO, Receipt, Billed quantities on invoice item for reference
        invoiceItem.setPoQty(poQty);
        invoiceItem.setReceivedQty(receiptQty);
        invoiceItem.setBilledQty(invoiceQty);

        return new MatchResult(overallStatus, totalQtyVariance, totalAmountVariance, mismatches);
    }

    private BigDecimal getPoQty(PurchaseInvoiceItem invoiceItem) {
        if (invoiceItem.getPurchaseOrderItem() != null) {
            return invoiceItem.getPurchaseOrderItem().getQty();
        } else if (invoiceItem.getPurchaseOrderItemId() != null) {
            return purchaseOrderItemRepository.findById(invoiceItem.getPurchaseOrderItemId())
                    .map(PurchaseOrderItem::getQty)
                    .orElse(null);
        }
        return null;
    }

    private BigDecimal getPoRate(PurchaseInvoiceItem invoiceItem) {
        if (invoiceItem.getPurchaseOrderItem() != null) {
            return invoiceItem.getPurchaseOrderItem().getRate();
        } else if (invoiceItem.getPurchaseOrderItemId() != null) {
            return purchaseOrderItemRepository.findById(invoiceItem.getPurchaseOrderItemId())
                    .map(PurchaseOrderItem::getRate)
                    .orElse(null);
        }
        return null;
    }

    private BigDecimal getReceiptQty(PurchaseInvoiceItem invoiceItem) {
        if (invoiceItem.getPurchaseReceiptItem() != null) {
            PurchaseReceiptItem pri = invoiceItem.getPurchaseReceiptItem();
            return pri.getQty().subtract(pri.getRejectedQty());
        } else if (invoiceItem.getPurchaseReceiptItemId() != null) {
            return purchaseReceiptItemRepository.findById(invoiceItem.getPurchaseReceiptItemId())
                    .map(pri -> pri.getQty().subtract(pri.getRejectedQty()))
                    .orElse(null);
        }
        return null;
    }

    private PurchaseInvoiceItem.MatchStatus determineMismatchStatus(PurchaseInvoiceItem.MatchStatus current, boolean qtyMismatch, boolean rateMismatch) {
        if (current == PurchaseInvoiceItem.MatchStatus.MATCHED) {
            if (qtyMismatch && rateMismatch) return PurchaseInvoiceItem.MatchStatus.BOTH_MISMATCH;
            if (qtyMismatch) return PurchaseInvoiceItem.MatchStatus.QUANTITY_MISMATCH;
            if (rateMismatch) return PurchaseInvoiceItem.MatchStatus.RATE_MISMATCH;
        } else if (current == PurchaseInvoiceItem.MatchStatus.QUANTITY_MISMATCH && rateMismatch) {
            return PurchaseInvoiceItem.MatchStatus.BOTH_MISMATCH;
        } else if (current == PurchaseInvoiceItem.MatchStatus.RATE_MISMATCH && qtyMismatch) {
            return PurchaseInvoiceItem.MatchStatus.BOTH_MISMATCH;
        }
        return current;
    }

    private void publishMatchedEvent(PurchaseInvoice invoice) {
        PurchaseInvoiceMatchedEvent event = new PurchaseInvoiceMatchedEvent(
                invoice.getId(),
                invoice.getCompanyId(),
                invoice.getSupplier().getId(),
                invoice.getInvoiceNo(),
                invoice.getGrandTotal(),
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, invoiceMatchedRoutingKey, event);
            log.debug("Published PurchaseInvoiceMatchedEvent for invoice: {}", invoice.getId());
        } catch (Exception e) {
            log.error("Failed to publish PurchaseInvoiceMatchedEvent for invoice: {}", invoice.getId(), e);
        }
    }

    private void publishMismatchEvent(PurchaseInvoice invoice, List<Output.MismatchDetail> mismatches) {
        List<PurchaseInvoiceMismatchEvent.MismatchDetail> eventMismatches = mismatches.stream()
                .map(m -> new PurchaseInvoiceMismatchEvent.MismatchDetail(
                        m.invoiceItemId(), m.type(), m.expected(), m.actual(), m.variance()))
                .collect(Collectors.toList());

        PurchaseInvoiceMismatchEvent event = new PurchaseInvoiceMismatchEvent(
                invoice.getId(),
                invoice.getCompanyId(),
                invoice.getSupplier().getId(),
                invoice.getInvoiceNo(),
                eventMismatches,
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, invoiceMismatchRoutingKey, event);
            log.debug("Published PurchaseInvoiceMismatchEvent for invoice: {}", invoice.getId());
        } catch (Exception e) {
            log.error("Failed to publish PurchaseInvoiceMismatchEvent for invoice: {}", invoice.getId(), e);
        }
    }

    private record MatchResult(
            PurchaseInvoiceItem.MatchStatus matchStatus,
            BigDecimal qtyVariance,
            BigDecimal amountVariance,
            List<Output.MismatchDetail> mismatches
    ) {}
}