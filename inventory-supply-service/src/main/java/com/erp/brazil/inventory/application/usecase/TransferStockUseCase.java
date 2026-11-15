package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.Item;
import com.erp.brazil.inventory.domain.model.StockLedgerEntry;
import com.erp.brazil.inventory.domain.model.Warehouse;
import com.erp.brazil.inventory.domain.repository.ItemRepository;
import com.erp.brazil.inventory.domain.repository.StockLedgerEntryRepository;
import com.erp.brazil.inventory.domain.repository.WarehouseRepository;
import com.erp.brazil.inventory.infrastructure.lock.RedissonDistributedLock;
import com.erp.brazil.inventory.infrastructure.messaging.event.StockTransferredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class TransferStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransferStockUseCase.class);

    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final RedissonDistributedLock distributedLock;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.stock-transferred:stock.transferred}")
    private String stockTransferredRoutingKey;

    public TransferStockUseCase(StockLedgerEntryRepository stockLedgerEntryRepository,
                                ItemRepository itemRepository,
                                WarehouseRepository warehouseRepository,
                                RedissonDistributedLock distributedLock,
                                RabbitTemplate rabbitTemplate) {
        this.stockLedgerEntryRepository = stockLedgerEntryRepository;
        this.itemRepository = itemRepository;
        this.warehouseRepository = warehouseRepository;
        this.distributedLock = distributedLock;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Input(
            UUID companyId,
            UUID itemId,
            UUID itemVariantId,
            UUID fromWarehouseId,
            UUID toWarehouseId,
            UUID fromLocationId,
            UUID toLocationId,
            BigDecimal qty,
            BigDecimal valuationRate,
            String referenceType,
            String referenceNo,
            UUID batchId,
            UUID serialNumberId,
            UUID userId
    ) {}

    public record Output(
            StockLedgerEntry outboundEntry,
            StockLedgerEntry inboundEntry
    ) {}

    public Output execute(Input input) {
        log.info("Transferring stock: item={} from={} to={} qty={}", input.itemId(), input.fromWarehouseId(), input.toWarehouseId(), input.qty());

        // Validate warehouses
        Warehouse fromWarehouse = warehouseRepository.findById(input.fromWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("From warehouse not found: " + input.fromWarehouseId()));
        Warehouse toWarehouse = warehouseRepository.findById(input.toWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("To warehouse not found: " + input.toWarehouseId()));

        if (!fromWarehouse.getCompanyId().equals(input.companyId()) ||
            !toWarehouse.getCompanyId().equals(input.companyId())) {
            throw new IllegalArgumentException("Warehouses must belong to the same company");
        }

        // Validate item
        Item item = itemRepository.findById(input.itemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + input.itemId()));

        // Check available stock in from warehouse
        BigDecimal availableQty = stockLedgerEntryRepository.getCurrentStockQty(
                input.companyId(), input.itemId(), input.fromWarehouseId());

        if (availableQty.compareTo(input.qty()) < 0) {
            if (!fromWarehouse.getAllowNegativeStock()) {
                throw new InsufficientStockException(
                        "Insufficient stock in from warehouse. Available: " + availableQty + ", Requested: " + input.qty());
            }
        }

        // Use distributed lock on both warehouses (acquire in consistent order to avoid deadlock)
        UUID firstLock = input.fromWarehouseId().compareTo(input.toWarehouseId()) < 0 ? input.fromWarehouseId() : input.toWarehouseId();
        UUID secondLock = firstLock.equals(input.fromWarehouseId()) ? input.toWarehouseId() : input.fromWarehouseId();

        String lockKey1 = "lock:inventory:" + input.itemId() + ":" + firstLock;
        String lockKey2 = "lock:inventory:" + input.itemId() + ":" + secondLock;

        return distributedLock.executeWithMultipleLocks(List.of(lockKey1, lockKey2), 30, TimeUnit.SECONDS, () -> {
            // Double-check stock after acquiring locks
            BigDecimal currentAvailableQty = stockLedgerEntryRepository.getCurrentStockQty(
                    input.companyId(), input.itemId(), input.fromWarehouseId());

            if (currentAvailableQty.compareTo(input.qty()) < 0 && !fromWarehouse.getAllowNegativeStock()) {
                throw new InsufficientStockException(
                        "Insufficient stock in from warehouse. Available: " + currentAvailableQty + ", Requested: " + input.qty());
            }

            // Create outbound entry (negative qty)
            StockLedgerEntry outboundEntry = createOutboundEntry(input, item, fromWarehouse);
            stockLedgerEntryRepository.save(outboundEntry);

            // Create inbound entry (positive qty)
            StockLedgerEntry inboundEntry = createInboundEntry(input, item, toWarehouse);
            stockLedgerEntryRepository.save(inboundEntry);

            // Publish event
            publishStockTransferredEvent(input, outboundEntry, inboundEntry);

            log.info("Stock transfer completed. Outbound: {}, Inbound: {}", outboundEntry.getId(), inboundEntry.getId());
            return new Output(outboundEntry, inboundEntry);
        });
    }

    private StockLedgerEntry createOutboundEntry(Input input, Item item, Warehouse fromWarehouse) {
        BigDecimal currentStockQty = stockLedgerEntryRepository.getCurrentStockQty(
                input.companyId(), input.itemId(), input.fromWarehouseId());
        BigDecimal currentStockValue = stockLedgerEntryRepository.getCurrentStockValue(
                input.companyId(), input.itemId(), input.fromWarehouseId());

        BigDecimal newStockQty = currentStockQty.subtract(input.qty());
        BigDecimal newStockValue = currentStockValue.subtract(input.qty().multiply(input.valuationRate()));

        return StockLedgerEntry.builder()
                .companyId(input.companyId())
                .itemId(input.itemId())
                .itemVariantId(input.itemVariantId())
                .warehouseId(input.fromWarehouseId())
                .locationId(input.fromLocationId())
                .batchId(input.batchId())
                .serialNumberId(input.serialNumberId())
                .qtyChange(input.qty().negate())
                .valuationRate(input.valuationRate())
                .stockValue(input.qty().multiply(input.valuationRate()).negate())
                .stockQtyAfter(newStockQty)
                .stockValueAfter(newStockValue)
                .voucherType(input.referenceType())
                .voucherNo(input.referenceNo())
                .voucherDetailNo("OUT")
                .postingDate(LocalDateTime.now())
                .postingTime(LocalDateTime.now())
                .actualQty(input.qty().negate())
                .outgoingRate(input.valuationRate())
                .valuationMethod(item.getValuationMethod())
                .build();
    }

    private StockLedgerEntry createInboundEntry(Input input, Item item, Warehouse toWarehouse) {
        BigDecimal currentStockQty = stockLedgerEntryRepository.getCurrentStockQty(
                input.companyId(), input.itemId(), input.toWarehouseId());
        BigDecimal currentStockValue = stockLedgerEntryRepository.getCurrentStockValue(
                input.companyId(), input.itemId(), input.toWarehouseId());

        BigDecimal newStockQty = currentStockQty.add(input.qty());
        BigDecimal newStockValue = currentStockValue.add(input.qty().multiply(input.valuationRate()));

        return StockLedgerEntry.builder()
                .companyId(input.companyId())
                .itemId(input.itemId())
                .itemVariantId(input.itemVariantId())
                .warehouseId(input.toWarehouseId())
                .locationId(input.toLocationId())
                .batchId(input.batchId())
                .serialNumberId(input.serialNumberId())
                .qtyChange(input.qty())
                .valuationRate(input.valuationRate())
                .stockValue(input.qty().multiply(input.valuationRate()))
                .stockQtyAfter(newStockQty)
                .stockValueAfter(newStockValue)
                .voucherType(input.referenceType())
                .voucherNo(input.referenceNo())
                .voucherDetailNo("IN")
                .postingDate(LocalDateTime.now())
                .postingTime(LocalDateTime.now())
                .actualQty(input.qty())
                .incomingRate(input.valuationRate())
                .valuationMethod(item.getValuationMethod())
                .build();
    }

    private void publishStockTransferredEvent(Input input, StockLedgerEntry outboundEntry, StockLedgerEntry inboundEntry) {
        StockTransferredEvent event = new StockTransferredEvent(
                UUID.randomUUID(), // Transfer ID
                input.companyId(),
                input.itemId(),
                input.itemVariantId(),
                input.fromWarehouseId(),
                input.toWarehouseId(),
                input.fromLocationId(),
                input.toLocationId(),
                input.qty(),
                input.valuationRate(),
                input.referenceType(),
                input.referenceNo(),
                input.batchId(),
                input.serialNumberId(),
                outboundEntry.getId(),
                inboundEntry.getId(),
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, stockTransferredRoutingKey, event);
            log.debug("Published StockTransferredEvent for transfer: {}", event.transferId());
        } catch (Exception e) {
            log.error("Failed to publish StockTransferredEvent", e);
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}