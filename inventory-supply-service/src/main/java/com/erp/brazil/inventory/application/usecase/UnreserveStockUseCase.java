package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.StockReservation;
import com.erp.brazil.inventory.domain.repository.StockReservationRepository;
import com.erp.brazil.inventory.infrastructure.lock.RedissonDistributedLock;
import com.erp.brazil.inventory.infrastructure.messaging.event.StockUnreservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UnreserveStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(UnreserveStockUseCase.class);

    private final StockReservationRepository stockReservationRepository;
    private final RedissonDistributedLock distributedLock;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.stock-unreserved:stock.unreserved}")
    private String stockUnreservedRoutingKey;

    public UnreserveStockUseCase(StockReservationRepository stockReservationRepository,
                                 RedissonDistributedLock distributedLock,
                                 RabbitTemplate rabbitTemplate) {
        this.stockReservationRepository = stockReservationRepository;
        this.distributedLock = distributedLock;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Input(UUID salesOrderId, String reason) {}

    public record Output(int cancelledCount) {}

    public Output execute(Input input) {
        log.info("Unreserving stock for sales order: {} - Reason: {}", input.salesOrderId(), input.reason());

        List<StockReservation> reservations = stockReservationRepository
                .findByCompanyIdAndVoucherTypeAndVoucherNo(
                        getCompanyIdFromSalesOrder(input.salesOrderId()), "SalesOrder", input.salesOrderId().toString());

        if (reservations.isEmpty()) {
            log.warn("No active reservations found for sales order: {}", input.salesOrderId());
            return new Output(0);
        }

        int cancelledCount = 0;

        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() == StockReservation.ReservationStatus.ACTIVE ||
                reservation.getStatus() == StockReservation.ReservationStatus.PARTIALLY_DELIVERED) {

                String lockKey = "lock:inventory:" + reservation.getItemId() + ":" + reservation.getWarehouseId();

                distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
                    // Refresh reservation
                    StockReservation freshReservation = stockReservationRepository.findById(reservation.getId())
                            .orElseThrow(() -> new IllegalStateException("Reservation not found: " + reservation.getId()));

                    if (freshReservation.getStatus() == StockReservation.ReservationStatus.ACTIVE ||
                        freshReservation.getStatus() == StockReservation.ReservationStatus.PARTIALLY_DELIVERED) {

                        freshReservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
                        stockReservationRepository.save(freshReservation);

                        publishStockUnreservedEvent(freshReservation, input.reason());
                        cancelledCount++;
                    }
                    return null;
                });
            }
        }

        log.info("Cancelled {} reservations for sales order: {}", cancelledCount, input.salesOrderId());
        return new Output(cancelledCount);
    }

    public Output executeByReservationIds(List<UUID> reservationIds, String reason) {
        log.info("Unreserving stock for {} reservations - Reason: {}", reservationIds.size(), reason);

        int cancelledCount = 0;

        for (UUID reservationId : reservationIds) {
            StockReservation reservation = stockReservationRepository.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

            String lockKey = "lock:inventory:" + reservation.getItemId() + ":" + reservation.getWarehouseId();

            distributedLock.executeWithLock(lockKey, 30, TimeUnit.SECONDS, () -> {
                StockReservation freshReservation = stockReservationRepository.findById(reservationId)
                        .orElseThrow(() -> new IllegalStateException("Reservation not found: " + reservationId));

                if (freshReservation.getStatus() == StockReservation.ReservationStatus.ACTIVE ||
                    freshReservation.getStatus() == StockReservation.ReservationStatus.PARTIALLY_DELIVERED) {

                    freshReservation.setStatus(StockReservation.ReservationStatus.CANCELLED);
                    stockReservationRepository.save(freshReservation);

                    publishStockUnreservedEvent(freshReservation, reason);
                    cancelledCount++;
                }
                return null;
            });
        }

        return new Output(cancelledCount);
    }

    private UUID getCompanyIdFromSalesOrder(UUID salesOrderId) {
        throw new UnsupportedOperationException("CompanyId resolution from SalesOrder not implemented");
    }

    private void publishStockUnreservedEvent(StockReservation reservation, String reason) {
        StockUnreservedEvent event = new StockUnreservedEvent(
                reservation.getId(),
                reservation.getCompanyId(),
                reservation.getItemId(),
                reservation.getItemVariantId(),
                reservation.getWarehouseId(),
                reservation.getQtyReserved().subtract(reservation.getQtyDelivered()),
                reservation.getVoucherType(),
                reservation.getVoucherNo(),
                reason,
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, stockUnreservedRoutingKey, event);
            log.debug("Published StockUnreservedEvent for reservation: {}", reservation.getId());
        } catch (Exception e) {
            log.error("Failed to publish StockUnreservedEvent for reservation: {}", reservation.getId(), e);
        }
    }
}