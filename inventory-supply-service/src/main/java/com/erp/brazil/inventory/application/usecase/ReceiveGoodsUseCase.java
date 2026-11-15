package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.Item;
import com.erp.brazil.inventory.domain.model.ItemBatch;
import com.erp.brazil.inventory.domain.model.ItemSerialNumber;
import com.erp.brazil.inventory.domain.model.PurchaseReceipt;
import com.erp.brazil.inventory.domain.model.PurchaseReceiptItem;
import com.erp.brazil.inventory.domain.model.SerialAndBatchBundle;
import com.erp.brazil.inventory.domain.model.StockLedgerEntry;
import com.erp.brazil.inventory.domain.repository.ItemBatchRepository;
import com.erp.brazil.inventory.domain.repository.ItemRepository;
import com.erp.brazil.inventory.domain.repository.ItemSerialNumberRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptItemRepository;
import com.erp.brazil.inventory.domain.repository.PurchaseReceiptRepository;
import com.erp.brazil.inventory.domain.repository.SerialAndBatchBundleRepository;
import com.erp.brazil.inventory.domain.repository.StockLedgerEntryRepository;
import com.erp.brazil.inventory.infrastructure.lock.RedissonDistributedLock;
import com.erp.brazil.inventory.infrastructure.messaging.event.GoodsReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReceiveGoodsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReceiveGoodsUseCase.class);

    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptItemRepository purchaseReceiptItemRepository;
    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final ItemBatchRepository itemBatchRepository;
    private final ItemSerialNumberRepository itemSerialNumberRepository;
    private final SerialAndBatchBundleRepository serialAndBatchBundleRepository;
    private final ItemRepository itemRepository;
    private final RedissonDistributedLock distributedLock;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.goods-received:goods.received}")
    private String goodsReceivedRoutingKey;

    public ReceiveGoodsUseCase(PurchaseReceiptRepository purchaseReceiptRepository,
                               PurchaseReceiptItemRepository purchaseReceiptItemRepository,
                               StockLedgerEntryRepository stockLedgerEntryRepository,
                               ItemBatchRepository itemBatchRepository,
                               ItemSerialNumberRepository itemSerialNumberRepository,
                               SerialAndBatchBundleRepository serialAndBatchBundleRepository,
                               ItemRepository itemRepository,
                               RedissonDistributedLock distributedLock,
                               RabbitTemplate rabbitTemplate) {
        this.purchaseReceiptRepository = purchaseReceiptRepository;
        this.purchaseReceiptItemRepository = purchaseReceiptItemRepository;
        this.stockLedgerEntryRepository = stockLedgerEntryRepository;
        this.itemBatchRepository = itemBatchRepository;
        this.itemSerialNumberRepository = itemSerialNumberRepository;
        this.serialAndBatchBundleRepository = serialAndBatchBundleRepository;
        this.itemRepository = itemRepository;
        this.distributedLock = distributedLock;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Input(UUID receiptId, UUID userId) {}

    public record Output(PurchaseReceipt receipt, List<StockLedgerEntry> ledgerEntries) {}

    public Output execute(Input input) {
        log.info("Receiving goods for receipt: {}", input.receiptId());

        PurchaseReceipt receipt = purchaseReceiptRepository.findById(input.receiptId())
                .orElseThrow(() -> new IllegalArgumentException("Purchase receipt not found: " + input.receiptId()));

        if (receipt.getStatus() != PurchaseReceipt.PRStatus.SUBMITTED &&
            receipt.getStatus() != PurchaseReceipt.PRStatus.QUALITY_INSPECTION) {
            throw new IllegalStateException("Receipt must be in SUBMITTED or QUALITY_INSPECTION status. Current: " + receipt.getStatus());
        }

        List<PurchaseReceiptItem> items = purchaseReceiptItemRepository.findByPurchaseReceiptId(input.receiptId());
        List<StockLedgerEntry> ledgerEntries = new ArrayList<>();

        for (PurchaseReceiptItem item : items) {
            if (item.getQty().compareTo(item.getRejectedQty()) <= 0) {
                continue; // Skip fully rejected items
            }

            BigDecimal acceptedQty = item.getQty().subtract(item.getRejectedQty());
            String lockKey = "lock:inventory:" + item.getItemId() + ":" + item.getWarehouseId();

            distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
                // Create Stock Ledger Entry
                StockLedgerEntry sle = createStockLedgerEntry(receipt, item, acceptedQty, input.userId());
                stockLedgerEntryRepository.save(sle);
                ledgerEntries.add(sle);

                // Update batch if item has batch
                if (item.getBatchNo() != null && !item.getBatchNo().isBlank()) {
                    updateItemBatch(receipt, item, acceptedQty, input.userId());
                }

                // Update serial numbers if item has serials
                if (item.getSerialNos() != null && !item.getSerialNos().isBlank()) {
                    updateSerialNumbers(receipt, item, acceptedQty, input.userId());
                }

                // Update serial and batch bundle
                updateSerialBatchBundle(receipt, item, acceptedQty, input.userId());

                return null;
            });
        }

        // Update receipt status
        receipt.setStatus(PurchaseReceipt.PRStatus.ACCEPTED);
        purchaseReceiptRepository.save(receipt);

        // Publish event
        publishGoodsReceivedEvent(receipt, ledgerEntries);

        log.info("Successfully received goods for receipt: {}. Created {} ledger entries.", input.receiptId(), ledgerEntries.size());
        return new Output(receipt, ledgerEntries);
    }

    private StockLedgerEntry createStockLedgerEntry(PurchaseReceipt receipt, PurchaseReceiptItem item, BigDecimal acceptedQty, UUID userId) {
        Item itemEntity = itemRepository.findById(item.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + item.getItemId()));

        BigDecimal valuationRate = item.getRate() != null ? item.getRate() : itemEntity.getStandardRate();

        BigDecimal currentStockQty = stockLedgerEntryRepository.getCurrentStockQty(
                receipt.getCompanyId(), item.getItemId(), item.getWarehouseId());
        BigDecimal currentStockValue = stockLedgerEntryRepository.getCurrentStockValue(
                receipt.getCompanyId(), item.getItemId(), item.getWarehouseId());

        BigDecimal newStockQty = currentStockQty.add(acceptedQty);
        BigDecimal newStockValue = currentStockValue.add(acceptedQty.multiply(valuationRate));

        return StockLedgerEntry.builder()
                .companyId(receipt.getCompanyId())
                .itemId(item.getItemId())
                .itemVariantId(item.getItemVariantId())
                .warehouseId(item.getWarehouseId())
                .locationId(item.getLocationId())
                .qtyChange(acceptedQty)
                .valuationRate(valuationRate)
                .stockValue(acceptedQty.multiply(valuationRate))
                .stockQtyAfter(newStockQty)
                .stockValueAfter(newStockValue)
                .voucherType("PurchaseReceipt")
                .voucherNo(receipt.getReceiptNo())
                .voucherDetailNo(item.getIdx().toString())
                .postingDate(receipt.getPostingDate().atStartOfDay())
                .postingTime(LocalDateTime.now())
                .actualQty(acceptedQty)
                .incomingRate(valuationRate)
                .valuationMethod(itemEntity.getValuationMethod())
                .build();
    }

    private void updateItemBatch(PurchaseReceipt receipt, PurchaseReceiptItem item, BigDecimal acceptedQty, UUID userId) {
        Optional<ItemBatch> existingBatch = itemBatchRepository.findActiveByItemAndBatchNo(
                receipt.getCompanyId(), item.getItemId(), item.getBatchNo());

        ItemBatch batch;
        if (existingBatch.isPresent()) {
            batch = existingBatch.get();
            batch.setQuantity(batch.getQuantity().add(acceptedQty));
            if (item.getValuationRate() != null) {
                batch.setValuationRate(item.getValuationRate());
            }
        } else {
            batch = ItemBatch.builder()
                    .companyId(receipt.getCompanyId())
                    .itemId(item.getItemId())
                    .itemVariantId(item.getItemVariantId())
                    .batchNo(item.getBatchNo())
                    .quantity(acceptedQty)
                    .valuationRate(item.getRate())
                    .manufacturingDate(item.getManufacturingDate() != null ? item.getManufacturingDate().toLocalDate() : null)
                    .expiryDate(item.getExpiryDate() != null ? item.getExpiryDate().toLocalDate() : null)
                    .qualityStatus(ItemBatch.QualityStatus.ACCEPTED)
                    .referenceDoctype("PurchaseReceipt")
                    .referenceName(receipt.getReceiptNo())
                    .supplierId(receipt.getSupplier().getId())
                    .isActive(true)
                    .build();
        }
        itemBatchRepository.save(batch);
    }

    private void updateSerialNumbers(PurchaseReceipt receipt, PurchaseReceiptItem item, BigDecimal acceptedQty, UUID userId) {
        String[] serialNos = item.getSerialNos().split(",");
        int count = 0;

        for (String serialNo : serialNos) {
            if (count >= acceptedQty.intValue()) break;

            String trimmedSerial = serialNo.trim();
            if (trimmedSerial.isBlank()) continue;

            Optional<ItemSerialNumber> existing = itemSerialNumberRepository
                    .findByCompanyIdAndItemIdAndSerialNo(receipt.getCompanyId(), item.getItemId(), trimmedSerial);

            ItemSerialNumber serial;
            if (existing.isPresent()) {
                serial = existing.get();
                serial.setStatus(ItemSerialNumber.SerialStatus.AVAILABLE);
                serial.setWarehouseId(item.getWarehouseId());
                serial.setLocationId(item.getLocationId());
            } else {
                serial = ItemSerialNumber.builder()
                        .companyId(receipt.getCompanyId())
                        .itemId(item.getItemId())
                        .itemVariantId(item.getItemVariantId())
                        .serialNo(trimmedSerial)
                        .status(ItemSerialNumber.SerialStatus.AVAILABLE)
                        .warehouseId(item.getWarehouseId())
                        .locationId(item.getLocationId())
                        .purchaseReceiptId(receipt.getId())
                        .build();
            }
            itemSerialNumberRepository.save(serial);
            count++;
        }
    }

    private void updateSerialBatchBundle(PurchaseReceipt receipt, PurchaseReceiptItem item, BigDecimal acceptedQty, UUID userId) {
        if (item.getBatchNo() == null || item.getBatchNo().isBlank()) {
            return;
        }

        Optional<ItemBatch> batchOpt = itemBatchRepository.findActiveByItemAndBatchNo(
                receipt.getCompanyId(), item.getItemId(), item.getBatchNo());

        if (batchOpt.isEmpty()) {
            return;
        }

        ItemBatch batch = batchOpt.get();

        if (item.getSerialNos() != null && !item.getSerialNos().isBlank()) {
            String[] serialNos = item.getSerialNos().split(",");
            int count = 0;

            for (String serialNo : serialNos) {
                if (count >= acceptedQty.intValue()) break;

                String trimmedSerial = serialNo.trim();
                if (trimmedSerial.isBlank()) continue;

                Optional<ItemSerialNumber> serialOpt = itemSerialNumberRepository
                        .findByCompanyIdAndItemIdAndSerialNo(receipt.getCompanyId(), item.getItemId(), trimmedSerial);

                if (serialOpt.isPresent()) {
                    ItemSerialNumber serial = serialOpt.get();

                    Optional<SerialAndBatchBundle> bundleOpt = serialAndBatchBundleRepository
                            .findByItemBatchSerial(receipt.getCompanyId(), item.getItemId(), batch.getId(), serial.getId());

                    SerialAndBatchBundle bundle;
                    if (bundleOpt.isPresent()) {
                        bundle = bundleOpt.get();
                        bundle.setQuantity(bundle.getQuantity().add(BigDecimal.ONE));
                    } else {
                        bundle = SerialAndBatchBundle.builder()
                                .companyId(receipt.getCompanyId())
                                .itemId(item.getItemId())
                                .batchId(batch.getId())
                                .serialNumberId(serial.getId())
                                .quantity(BigDecimal.ONE)
                                .warehouseId(item.getWarehouseId())
                                .locationId(item.getLocationId())
                                .voucherType("PurchaseReceipt")
                                .voucherNo(receipt.getReceiptNo())
                                .build();
                    }
                    serialAndBatchBundleRepository.save(bundle);
                    count++;
                }
            }
        } else {
            // No serial numbers, just update batch quantity bundle
            Optional<SerialAndBatchBundle> bundleOpt = serialAndBatchBundleRepository
                    .findByItemBatchSerial(receipt.getCompanyId(), item.getItemId(), batch.getId(), null);

            // For non-serialized items with batches, we don't create bundles
        }
    }

    private void publishGoodsReceivedEvent(PurchaseReceipt receipt, List<StockLedgerEntry> ledgerEntries) {
        List<GoodsReceivedEvent.ItemDetail> itemDetails = ledgerEntries.stream()
                .map(sle -> new GoodsReceivedEvent.ItemDetail(
                        sle.getItemId(),
                        sle.getItemVariantId(),
                        sle.getWarehouseId(),
                        sle.getQtyChange(),
                        sle.getValuationRate(),
                        sle.getBatchId(),
                        sle.getSerialNumberId()
                ))
                .collect(Collectors.toList());

        GoodsReceivedEvent event = new GoodsReceivedEvent(
                receipt.getId(),
                receipt.getCompanyId(),
                receipt.getSupplier().getId(),
                receipt.getReceiptNo(),
                receipt.getReceiptDate(),
                receipt.getPurchaseOrder() != null ? receipt.getPurchaseOrder().getId() : null,
                itemDetails,
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, goodsReceivedRoutingKey, event);
            log.debug("Published GoodsReceivedEvent for receipt: {}", receipt.getId());
        } catch (Exception e) {
            log.error("Failed to publish GoodsReceivedEvent for receipt: {}", receipt.getId(), e);
        }
    }
}