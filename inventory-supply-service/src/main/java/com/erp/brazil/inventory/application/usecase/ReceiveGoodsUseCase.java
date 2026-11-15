package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.*;
import com.erp.brazil.inventory.domain.repository.*;
import com.erp.brazil.inventory.infrastructure.lock.RedissonDistributedLock;
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
public class ReceiveGoodsUseCase {

    private final PurchaseReceiptRepository prRepository;
    private final PurchaseReceiptItemRepository priRepository;
    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderItemRepository poiRepository;
    private final ItemRepository itemRepository;
    private final ItemBatchRepository batchRepository;
    private final ItemSerialNumberRepository serialRepository;
    private final StockLedgerEntryRepository sleRepository;
    private final StockReservationRepository reservationRepository;
    private final SerialAndBatchBundleRepository bundleRepository;
    private final RedissonDistributedLock distributedLock;

    @Transactional
    public PurchaseReceipt receiveGoods(UUID companyId, UUID prId, List<ReceiveItemRequest> items) {
        PurchaseReceipt receipt = prRepository.findById(prId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase Receipt not found: " + prId));

        if (receipt.getStatus() == PurchaseReceipt.PRStatus.COMPLETED || receipt.getStatus() == PurchaseReceipt.PRStatus.CANCELLED) {
            throw new IllegalStateException("Cannot receive goods for receipt in status: " + receipt.getStatus());
        }

        boolean allReceived = true;
        for (ReceiveItemRequest req : items) {
            PurchaseReceiptItem pri = priRepository.findById(req.getPriId())
                    .orElseThrow(() -> new IllegalArgumentException("PR Item not found: " + req.getPriId()));

            Item item = itemRepository.findById(pri.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + pri.getItemId()));

            // Process each item with distributed lock
            String lockKey = "lock:inventory:" + item.getId() + ":" + receipt.getWarehouseId();
            distributedLock.executeWithLock(lockKey, () -> {
                processReceiptItem(receipt, pri, req, item);
            });

            if (pri.getQty().compareTo(pri.getRejectedQty()) > 0) {
                allReceived = false;
            }
        }

        // Update receipt status
        if (allReceived) {
            receipt.setStatus(PurchaseReceipt.PRStatus.COMPLETED);
        } else {
            receipt.setStatus(PurchaseReceipt.PRStatus.PARTIALLY_RECEIVED);
        }

        // Update linked PO status
        if (receipt.getPurchaseOrderId() != null) {
            updatePOStatus(receipt.getPurchaseOrderId());
        }

        return prRepository.save(receipt);
    }

    private void processReceiptItem(PurchaseReceipt receipt, PurchaseReceiptItem pri, ReceiveItemRequest req, Item item) {
        // Create SLE for stock increase
        BigDecimal acceptedQty = req.getAcceptedQty();
        if (acceptedQty.compareTo(BigDecimal.ZERO) > 0) {
            createStockLedgerEntry(receipt, pri, acceptedQty, item);
        }

        // Handle batch tracking
        if (item.getHasBatch() && req.getBatchNo() != null) {
            handleBatch(pri, req, item);
        }

        // Handle serial numbers
        if (item.getHasSerialNo() && req.getSerialNos() != null && !req.getSerialNos().isEmpty()) {
            handleSerialNumbers(pri, req, item, receipt);
        }

        // Update PR item
        pri.setQty(acceptedQty.add(req.getRejectedQty()));
        pri.setRejectedQty(req.getRejectedQty());
        pri.setQualityStatus(req.getQualityStatus());
        pri.setBatchNo(req.getBatchNo());
        pri.setSerialNos(req.getSerialNos() != null ? String.join(",", req.getSerialNos()) : null);
        pri.setManufacturingDate(req.getManufacturingDate());
        pri.setExpiryDate(req.getExpiryDate());
        priRepository.save(pri);

        // Release any linked reservations
        releaseReservations(receipt, pri);
    }

    private void createStockLedgerEntry(PurchaseReceipt receipt, PurchaseReceiptItem pri, BigDecimal qty, Item item) {
        // Get last valuation rate for FIFO/Moving Average
        StockLedgerEntry lastEntry = sleRepository.findLastEntry(
                receipt.getCompanyId(), pri.getItemId(), receipt.getWarehouseId()).orElse(null);

        BigDecimal valuationRate = pri.getRate();
        if (item.getValuationMethod() == Item.ValuationMethod.FIFO || item.getValuationMethod() == Item.ValuationMethod.MOVING_AVERAGE) {
            if (lastEntry != null) {
                valuationRate = calculateValuationRate(item, lastEntry, pri.getRate(), qty);
            }
        }

        BigDecimal stockValue = valuationRate.multiply(qty);

        // Get current stock after this entry
        BigDecimal currentStockQty = sleRepository.getCurrentStockQty(receipt.getCompanyId(), pri.getItemId(), receipt.getWarehouseId());
        BigDecimal currentStockValue = sleRepository.getCurrentStockValue(receipt.getCompanyId(), pri.getItemId(), receipt.getWarehouseId());

        StockLedgerEntry sle = StockLedgerEntry.builder()
                .companyId(receipt.getCompanyId())
                .itemId(pri.getItemId())
                .itemVariantId(pri.getItemVariantId())
                .warehouseId(receipt.getWarehouseId())
                .locationId(pri.getLocationId())
                .batchId(pri.getBatchId())
                .serialNumberId(pri.getSerialNumberId())
                .qtyChange(qty)
                .valuationRate(valuationRate)
                .stockValue(stockValue)
                .stockQtyAfter(currentStockQty.add(qty))
                .stockValueAfter(currentStockValue.add(stockValue))
                .voucherType("Purchase Receipt")
                .voucherNo(receipt.getReceiptNo())
                .voucherDetailNo(pri.getId().toString())
                .postingDate(receipt.getPostingDate().atStartOfDay())
                .postingTime(LocalDateTime.now())
                .actualQty(qty)
                .incomingRate(valuationRate)
                .valuationMethod(item.getValuationMethod())
                .build();

        sleRepository.save(sle);
    }

    private BigDecimal calculateValuationRate(Item item, StockLedgerEntry lastEntry, BigDecimal incomingRate, BigDecimal incomingQty) {
        if (item.getValuationMethod() == Item.ValuationMethod.FIFO) {
            return incomingRate; // FIFO uses actual incoming rate
        } else if (item.getValuationMethod() == Item.ValuationMethod.MOVING_AVERAGE) {
            BigDecimal currentQty = lastEntry.getStockQtyAfter();
            BigDecimal currentValue = lastEntry.getStockValueAfter();
            if (currentQty.compareTo(BigDecimal.ZERO) > 0) {
                return currentValue.add(incomingRate.multiply(incomingQty)).divide(currentQty.add(incomingQty), 6, BigDecimal.ROUND_HALF_UP);
            }
            return incomingRate;
        }
        return incomingRate;
    }

    private void handleBatch(PurchaseReceiptItem pri, ReceiveItemRequest req, Item item) {
        ItemBatch batch = batchRepository.findByItemIdAndBatchNo(pri.getItemId(), req.getBatchNo()).orElseGet(() -> {
            ItemBatch newBatch = ItemBatch.builder()
                    .companyId(pri.getCompanyId())
                    .itemId(pri.getItemId())
                    .itemVariantId(pri.getItemVariantId())
                    .batchNo(req.getBatchNo())
                    .manufacturingDate(req.getManufacturingDate() != null ? req.getManufacturingDate().toLocalDate() : null)
                    .expiryDate(req.getExpiryDate() != null ? req.getExpiryDate().toLocalDate() : null)
                    .quantity(BigDecimal.ZERO)
                    .valuationRate(req.getRate())
                    .referenceDoctype("Purchase Receipt")
                    .referenceName(pri.getPurchaseReceipt().getReceiptNo())
                    .qualityStatus(req.getQualityStatus())
                    .build();
            return batchRepository.save(newBatch);
        });

        // Update batch quantity
        batch.setQuantity(batch.getQuantity().add(req.getAcceptedQty()));
        if (req.getRate() != null) {
            batch.setValuationRate(req.getRate());
        }
        batchRepository.save(batch);

        pri.setBatchId(batch.getId());
    }

    private void handleSerialNumbers(PurchaseReceiptItem pri, ReceiveItemRequest req, Item item, PurchaseReceipt receipt) {
        for (String serialNo : req.getSerialNos()) {
            ItemSerialNumber serial = serialRepository.findByItemIdAndSerialNo(pri.getItemId(), serialNo).orElseGet(() -> {
                ItemSerialNumber newSerial = ItemSerialNumber.builder()
                        .companyId(pri.getCompanyId())
                        .itemId(pri.getItemId())
                        .itemVariantId(pri.getItemVariantId())
                        .batchId(pri.getBatchId())
                        .serialNo(serialNo)
                        .status(ItemSerialNumber.SerialStatus.AVAILABLE)
                        .warehouseId(receipt.getWarehouseId())
                        .locationId(pri.getLocationId())
                        .purchaseReceiptId(receipt.getId())
                        .build();
                return serialRepository.save(newSerial);
            });

            // Create bundle
            SerialAndBatchBundle bundle = SerialAndBatchBundle.builder()
                    .companyId(pri.getCompanyId())
                    .itemId(pri.getItemId())
                    .batchId(pri.getBatchId())
                    .serialNumberId(serial.getId())
                    .quantity(BigDecimal.ONE)
                    .warehouseId(receipt.getWarehouseId())
                    .locationId(pri.getLocationId())
                    .voucherType("Purchase Receipt")
                    .voucherNo(receipt.getReceiptNo())
                    .build();
            bundleRepository.save(bundle);

            // Update serial status
            serial.setStatus(ItemSerialNumber.SerialStatus.AVAILABLE);
            serial.setWarehouseId(receipt.getWarehouseId());
            serial.setLocationId(pri.getLocationId());
            serial.setPurchaseReceiptId(receipt.getId());
            serialRepository.save(serial);
        }
    }

    private void releaseReservations(PurchaseReceipt receipt, PurchaseReceiptItem pri) {
        if (receipt.getPurchaseOrderId() != null && pri.getPurchaseOrderItemId() != null) {
            List<StockReservation> reservations = reservationRepository.findByVoucherTypeAndVoucherNo("Purchase Order", receipt.getPurchaseOrderId().toString());
            for (StockReservation res : reservations) {
                if (res.getItemId().equals(pri.getItemId()) && res.getStatus() == StockReservation.ReservationStatus.ACTIVE) {
                    BigDecimal deliverQty = pri.getQty().min(res.getQtyReserved().subtract(res.getQtyDelivered()));
                    res.setQtyDelivered(res.getQtyDelivered().add(deliverQty));
                    if (res.getQtyDelivered().compareTo(res.getQtyReserved()) >= 0) {
                        res.setStatus(StockReservation.ReservationStatus.DELIVERED);
                    } else {
                        res.setStatus(StockReservation.ReservationStatus.PARTIALLY_DELIVERED);
                    }
                    reservationRepository.save(res);
                }
            }
        }
    }

    private void updatePOStatus(UUID poId) {
        PurchaseOrder po = poRepository.findById(poId).orElse(null);
        if (po == null) return;

        List<PurchaseOrderItem> items = poiRepository.findByPurchaseOrderIdOrderByIdxAsc(poId);
        boolean allReceived = items.stream().allMatch(i -> i.getReceivedQty().compareTo(i.getQty()) >= 0);
        boolean anyReceived = items.stream().anyMatch(i -> i.getReceivedQty().compareTo(BigDecimal.ZERO) > 0);

        if (allReceived) {
            po.setStatus(PurchaseOrder.POStatus.RECEIVED);
        } else if (anyReceived) {
            po.setStatus(PurchaseOrder.POStatus.PARTIALLY_RECEIVED);
        }
        poRepository.save(po);
    }

    public static class ReceiveItemRequest {
        private UUID priId;
        private BigDecimal acceptedQty;
        private BigDecimal rejectedQty;
        private ItemBatch.QualityStatus qualityStatus;
        private String batchNo;
        private List<String> serialNos;
        private LocalDateTime manufacturingDate;
        private LocalDateTime expiryDate;
        private BigDecimal rate;

        // Getters and setters
        public UUID getPriId() { return priId; }
        public void setPriId(UUID priId) { this.priId = priId; }
        public BigDecimal getAcceptedQty() { return acceptedQty; }
        public void setAcceptedQty(BigDecimal acceptedQty) { this.acceptedQty = acceptedQty; }
        public BigDecimal getRejectedQty() { return rejectedQty; }
        public void setRejectedQty(BigDecimal rejectedQty) { this.rejectedQty = rejectedQty; }
        public ItemBatch.QualityStatus getQualityStatus() { return qualityStatus; }
        public void setQualityStatus(ItemBatch.QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public List<String> getSerialNos() { return serialNos; }
        public void setSerialNos(List<String> serialNos) { this.serialNos = serialNos; }
        public LocalDateTime getManufacturingDate() { return manufacturingDate; }
        public void setManufacturingDate(LocalDateTime manufacturingDate) { this.manufacturingDate = manufacturingDate; }
        public LocalDateTime getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
    }
}