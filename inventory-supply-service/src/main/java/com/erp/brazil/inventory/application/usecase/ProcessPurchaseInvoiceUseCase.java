package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.*;
import com.erp.brazil.inventory.domain.repository.*;
import com.erp.brazil.inventory.infrastructure.messaging.ThreeWayMatchEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPurchaseInvoiceUseCase {

    private final PurchaseInvoiceRepository piRepository;
    private final PurchaseInvoiceItemRepository piiRepository;
    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository poiRepository;
    private final PurchaseReceiptRepository prRepository;
    private final PurchaseReceiptItemRepository priRepository;
    private final ItemRepository itemRepository;
    private final ThreeWayMatchEventPublisher eventPublisher;

    @Transactional
    public PurchaseInvoice processInvoice(UUID companyId, UUID piId) {
        PurchaseInvoice invoice = piRepository.findById(piId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Invoice not found: " + piId));

        if (invoice.getStatus() != PurchaseInvoice.PIStatus.DRAFT && invoice.getStatus() != PurchaseInvoice.PIStatus.SUBMITTED) {
            throw new IllegalStateException("Invoice cannot be processed in status: " + invoice.getStatus());
        }

        // Perform 3-way match
        ThreeWayMatchResult matchResult = performThreeWayMatch(invoice);

        // Update invoice items with match results
        for (PurchaseInvoiceItem pii : invoice.getItems()) {
            ThreeWayMatchResult.ItemMatchResult itemResult = matchResult.getItemResults().get(pii.getId());
            if (itemResult != null) {
                pii.setMatchStatus(itemResult.getMatchStatus());
                pii.setPoQty(itemResult.getPoQty());
                pii.setReceivedQty(itemResult.getReceivedQty());
                pii.setVarianceQty(itemResult.getVarianceQty());
                pii.setVarianceAmount(itemResult.getVarianceAmount());
                piiRepository.save(pii);
            }
        }

        // Determine overall invoice status
        if (matchResult.isFullyMatched()) {
            invoice.setStatus(PurchaseInvoice.PIStatus.VALIDATED);
        } else if (matchResult.hasMismatch()) {
            invoice.setStatus(PurchaseInvoice.PIStatus.BLOCKED);
            invoice.setIsBlockedForPayment(true);
            invoice.setBlockReason("3-way match failed: " + matchResult.getSummary());
        }

        invoice.setTotalQty(matchResult.getTotalQty());
        invoice.setTotalAmount(matchResult.getTotalAmount());
        invoice.setTaxAmount(matchResult.getTotalTax());
        invoice.setGrandTotal(matchResult.getGrandTotal());

        PurchaseInvoice saved = piRepository.save(invoice);

        // Publish events
        if (matchResult.isFullyMatched()) {
            eventPublisher.publishMatched(saved);
        } else if (matchResult.hasMismatch()) {
            eventPublisher.publishMismatch(saved, matchResult);
        }

        return saved;
    }

    private ThreeWayMatchResult performThreeWayMatch(PurchaseInvoice invoice) {
        ThreeWayMatchResult result = new ThreeWayMatchResult();
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (PurchaseInvoiceItem pii : invoice.getItems()) {
            ThreeWayMatchResult.ItemMatchResult itemResult = matchItem(invoice, pii);
            result.getItemResults().put(pii.getId(), itemResult);

            totalQty = totalQty.add(pii.getQty());
            totalAmount = totalAmount.add(pii.getAmount());
            totalTax = totalTax.add(pii.getTaxAmount());

            if (itemResult.getMatchStatus() != PurchaseInvoiceItem.MatchStatus.MATCHED) {
                result.setHasMismatch(true);
            }
        }

        result.setTotalQty(totalQty);
        result.setTotalAmount(totalAmount);
        result.setTotalTax(totalTax);
        result.setGrandTotal(totalAmount.add(totalTax));
        result.setFullyMatched(!result.isHasMismatch());

        return result;
    }

    private ThreeWayMatchResult.ItemMatchResult matchItem(PurchaseInvoice invoice, PurchaseInvoiceItem pii) {
        ThreeWayMatchResult.ItemMatchResult result = new ThreeWayMatchResult.ItemMatchResult();

        // Get PO item
        PurchaseOrderItem poi = null;
        if (pii.getPurchaseOrderItemId() != null) {
            poi = poiRepository.findById(pii.getPurchaseOrderItemId()).orElse(null);
        } else if (invoice.getPurchaseOrderId() != null) {
            List<PurchaseOrderItem> poiItems = poiRepository.findByPurchaseOrderIdAndItemId(invoice.getPurchaseOrderId(), pii.getItemId());
            if (!poiItems.isEmpty()) {
                poi = poiItems.get(0);
            }
        }

        // Get PR item
        PurchaseReceiptItem pri = null;
        if (pii.getPurchaseReceiptItemId() != null) {
            pri = priRepository.findById(pii.getPurchaseReceiptItemId()).orElse(null);
        } else if (invoice.getPurchaseReceiptId() != null) {
            List<PurchaseReceiptItem> priItems = priRepository.findByPurchaseReceiptIdAndItemId(invoice.getPurchaseReceiptId(), pii.getItemId());
            if (!priItems.isEmpty()) {
                pri = priItems.get(0);
            }
        }

        BigDecimal poQty = poi != null ? poi.getQty() : BigDecimal.ZERO;
        BigDecimal receivedQty = pri != null ? pri.getQty() : BigDecimal.ZERO;
        BigDecimal billedQty = pii.getQty();

        result.setPoQty(poQty);
        result.setReceivedQty(receivedQty);

        BigDecimal qtyVariance = billedQty.subtract(receivedQty);
        BigDecimal rateVariance = BigDecimal.ZERO;

        if (poi != null) {
            rateVariance = pii.getRate().subtract(poi.getRate());
        }

        result.setVarianceQty(qtyVariance);
        result.setVarianceAmount(qtyVariance.multiply(pii.getRate()).add(rateVariance.multiply(billedQty)));

        // Determine match status
        if (qtyVariance.compareTo(BigDecimal.ZERO) == 0 && rateVariance.compareTo(BigDecimal.ZERO) == 0) {
            result.setMatchStatus(PurchaseInvoiceItem.MatchStatus.MATCHED);
        } else if (qtyVariance.compareTo(BigDecimal.ZERO) != 0 && rateVariance.compareTo(BigDecimal.ZERO) != 0) {
            result.setMatchStatus(PurchaseInvoiceItem.MatchStatus.BOTH_MISMATCH);
        } else if (qtyVariance.compareTo(BigDecimal.ZERO) != 0) {
            result.setMatchStatus(PurchaseInvoiceItem.MatchStatus.QUANTITY_MISMATCH);
        } else {
            result.setMatchStatus(PurchaseInvoiceItem.MatchStatus.RATE_MISMATCH);
        }

        // Check over-billing
        if (poi != null && billedQty.compareTo(poi.getQty()) > 0) {
            result.setMatchStatus(PurchaseInvoiceItem.MatchStatus.OVER_BILLED);
        }

        return result;
    }

    // Inner classes for match result
    public static class ThreeWayMatchResult {
        private boolean fullyMatched = false;
        private boolean hasMismatch = false;
        private BigDecimal totalQty;
        private BigDecimal totalAmount;
        private BigDecimal totalTax;
        private BigDecimal grandTotal;
        private java.util.Map<UUID, ItemMatchResult> itemResults = new java.util.HashMap<>();
        private String summary;

        public boolean isFullyMatched() { return fullyMatched; }
        public void setFullyMatched(boolean fullyMatched) { this.fullyMatched = fullyMatched; }
        public boolean isHasMismatch() { return hasMismatch; }
        public void setHasMismatch(boolean hasMismatch) { this.hasMismatch = hasMismatch; }
        public BigDecimal getTotalQty() { return totalQty; }
        public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getTotalTax() { return totalTax; }
        public void setTotalTax(BigDecimal totalTax) { this.totalTax = totalTax; }
        public BigDecimal getGrandTotal() { return grandTotal; }
        public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
        public java.util.Map<UUID, ItemMatchResult> getItemResults() { return itemResults; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public static class ItemMatchResult {
            private PurchaseInvoiceItem.MatchStatus matchStatus;
            private BigDecimal poQty;
            private BigDecimal receivedQty;
            private BigDecimal varianceQty;
            private BigDecimal varianceAmount;

            public PurchaseInvoiceItem.MatchStatus getMatchStatus() { return matchStatus; }
            public void setMatchStatus(PurchaseInvoiceItem.MatchStatus matchStatus) { this.matchStatus = matchStatus; }
            public BigDecimal getPoQty() { return poQty; }
            public void setPoQty(BigDecimal poQty) { this.poQty = poQty; }
            public BigDecimal getReceivedQty() { return receivedQty; }
            public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }
            public BigDecimal getVarianceQty() { return varianceQty; }
            public void setVarianceQty(BigDecimal varianceQty) { this.varianceQty = varianceQty; }
            public BigDecimal getVarianceAmount() { return varianceAmount; }
            public void setVarianceAmount(BigDecimal varianceAmount) { this.varianceAmount = varianceAmount; }
        }
    }
}