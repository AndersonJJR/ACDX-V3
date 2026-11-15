package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.Item;
import com.erp.brazil.inventory.domain.model.StockLedgerEntry;
import com.erp.brazil.inventory.domain.model.StockReservation;
import com.erp.brazil.inventory.domain.repository.ItemRepository;
import com.erp.brazil.inventory.domain.repository.StockLedgerEntryRepository;
import com.erp.brazil.inventory.domain.repository.StockReservationRepository;
import com.erp.brazil.inventory.infrastructure.lock.RedissonDistributedLock;
import com.erp.brazil.inventory.infrastructure.messaging.event.StockReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReserveStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReserveStockUseCase.class);

    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final StockReservationRepository stockReservationRepository;
    private final ItemRepository itemRepository;
    private final RedissonDistributedLock distributedLock;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.stock-reserved:stock.reserved}")
    private String stockReservedRoutingKey;

    // Cache for available quantities to avoid repeated DB calls within a single request
    private final Map<String, BigDecimal> availableQtyCache = new ConcurrentHashMap<>();

    public ReserveStockUseCase(StockLedgerEntryRepository stockLedgerEntryRepository,
                               StockReservationRepository stockReservationRepository,
                               ItemRepository itemRepository,
                               RedissonDistributedLock distributedLock,
                               RabbitTemplate rabbitTemplate) {
        this.stockLedgerEntryRepository = stockLedgerEntryRepository;
        this.stockReservationRepository = stockReservationRepository;
        this.itemRepository = itemRepository;
        this.distributedLock = distributedLock;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Input(UUID salesOrderId, List<ReservationItem> items) {
        public record ReservationItem(UUID itemId, UUID warehouseId, BigDecimal qty, UUID itemVariantId) {}
    }

    public record Output(List<StockReservation> reservations) {}

    public Output execute(Input input) {
        log.info("Reserving stock for sales order: {}", input.salesOrderId());
        availableQtyCache.clear();

        var reservations = input.items().stream()
                .map(item -> reserveSingleItem(input.salesOrderId(), item))
                .toList();

        // Publish events for all reservations
        reservations.forEach(this::publishStockReservedEvent);

        log.info("Successfully reserved {} items for sales order: {}", reservations.size(), input.salesOrderId());
        return new Output(reservations);
    }

    private StockReservation reserveSingleItem(UUID salesOrderId, Input.ReservationItem item) {
        String lockKey = "lock:inventory:" + item.itemId() + ":" + item.warehouseId();

        return distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
            // Check available quantity
            BigDecimal availableQty = getAvailableQuantity(item.itemId(), item.warehouseId(), item.itemVariantId());

            if (availableQty.compareTo(item.qty()) < 0) {
                throw new InsufficientStockException(
                        "Insufficient stock for item " + item.itemId() + " in warehouse " + item.warehouseId() +
                        ". Available: " + availableQty + ", Requested: " + item.qty());
            }

            // Check if item allows negative stock
            Item itemEntity = itemRepository.findById(item.itemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + item.itemId()));

            if (!itemEntity.getAllowNegativeStock() && availableQty.compareTo(item.qty()) < 0) {
                throw new InsufficientStockException("Negative stock not allowed for item: " + item.itemId());
            }

            // Create reservation
            StockReservation reservation = StockReservation.builder()
                    .companyId(getCompanyIdFromSalesOrder(salesOrderId)) // Would need to be passed or fetched
                    .itemId(item.itemId())
                    .itemVariantId(item.itemVariantId())
                    .warehouseId(item.warehouseId())
                    .qtyReserved(item.qty())
                    .status(StockReservation.ReservationStatus.ACTIVE)
                    .voucherType("SalesOrder")
                    .voucherNo(salesOrderId.toString())
                    .expiryDate(LocalDateTime.now().plusDays(30)) // Default 30 days expiry
                    .referenceDoctype("SalesOrder")
                    .referenceName(salesOrderId.toString())
                    .build();

            return stockReservationRepository.save(reservation);
        });
    }

    private BigDecimal getAvailableQuantity(UUID itemId, UUID warehouseId, UUID itemVariantId) {
        String cacheKey = itemId + ":" + warehouseId + ":" + (itemVariantId != null ? itemVariantId : "none");

        return availableQtyCache.computeIfAbsent(cacheKey, k -> {
            BigDecimal stockQty = stockLedgerEntryRepository.getCurrentStockQty(
                    getCompanyIdForItem(itemId), itemId, warehouseId);

            List<StockReservation.ReservationStatus> activeStatuses = List.of(
                    StockReservation.ReservationStatus.ACTIVE,
                    StockReservation.ReservationStatus.PARTIALLY_DELIVERED
            );

            BigDecimal reservedQty = stockReservationRepository.getTotalReservedQty(
                    getCompanyIdForItem(itemId), itemId, warehouseId, activeStatuses);

            return stockQty.subtract(reservedQty);
        });
    }

    private UUID getCompanyIdForItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .map(Item::getCompanyId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
    }

    private UUID getCompanyIdFromSalesOrder(UUID salesOrderId) {
        // In a real implementation, this would fetch from sales-order-service
        // For now, we'll need to pass companyId in the input or fetch from a shared context
        throw new UnsupportedOperationException("CompanyId resolution from SalesOrder not implemented - pass companyId in input");
    }

    private void publishStockReservedEvent(StockReservation reservation) {
        StockReservedEvent event = new StockReservedEvent(
                reservation.getId(),
                reservation.getCompanyId(),
                reservation.getItemId(),
                reservation.getItemVariantId(),
                reservation.getWarehouseId(),
                reservation.getQtyReserved(),
                reservation.getVoucherType(),
                reservation.getVoucherNo(),
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, stockReservedRoutingKey, event);
            log.debug("Published StockReservedEvent for reservation: {}", reservation.getId());
        } catch (Exception e) {
            log.error("Failed to publish StockReservedEvent for reservation: {}", reservation.getId(), e);
            // In production, consider using transactional outbox pattern
        }
    }

    public static class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }
}