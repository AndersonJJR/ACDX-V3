package com.erp.brazil.inventory.application.usecase;

import com.erp.brazil.inventory.domain.model.Item;
import com.erp.brazil.inventory.domain.model.RepostingJob;
import com.erp.brazil.inventory.domain.model.StockLedgerEntry;
import com.erp.brazil.inventory.domain.repository.ItemRepository;
import com.erp.brazil.inventory.domain.repository.RepostingJobRepository;
import com.erp.brazil.inventory.domain.repository.StockLedgerEntryRepository;
import com.erp.brazil.inventory.infrastructure.messaging.event.ValuationRecalculatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
public class RecalculateValuationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecalculateValuationUseCase.class);

    private final StockLedgerEntryRepository stockLedgerEntryRepository;
    private final RepostingJobRepository repostingJobRepository;
    private final ItemRepository itemRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange.inventory:inventory.exchange}")
    private String inventoryExchange;

    @Value("${app.messaging.routing-key.valuation-recalculated:valuation.recalculated}")
    private String valuationRecalculatedRoutingKey;

    @Value("${app.reposting.chunk-size:1000}")
    private int defaultChunkSize;

    public RecalculateValuationUseCase(StockLedgerEntryRepository stockLedgerEntryRepository,
                                       RepostingJobRepository repostingJobRepository,
                                       ItemRepository itemRepository,
                                       RabbitTemplate rabbitTemplate) {
        this.stockLedgerEntryRepository = stockLedgerEntryRepository;
        this.repostingJobRepository = repostingJobRepository;
        this.itemRepository = itemRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public record Input(UUID jobId) {}

    public record Output(RepostingJob job, long processedCount, long failedCount) {}

    public Output execute(Input input) {
        log.info("Starting valuation recalculation for job: {}", input.jobId());

        RepostingJob job = repostingJobRepository.findById(input.jobId())
                .orElseThrow(() -> new IllegalArgumentException("Reposting job not found: " + input.jobId()));

        if (job.getStatus() != RepostingJob.RepostingStatus.PENDING &&
            job.getStatus() != RepostingJob.RepostingStatus.QUEUED) {
            throw new IllegalStateException("Job must be in PENDING or QUEUED status. Current: " + job.getStatus());
        }

        job.setStatus(RepostingJob.RepostingStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        repostingJobRepository.save(job);

        AtomicLong processedCount = new AtomicLong(0);
        AtomicLong failedCount = new AtomicLong(0);
        List<UUID> repostedEntryIds = new ArrayList<>();

        try {
            // Determine items to process
            List<Item> itemsToProcess = getItemsToProcess(job);

            for (Item item : itemsToProcess) {
                if (job.getStatus() == RepostingJob.RepostingStatus.CANCELLED) {
                    log.info("Job {} was cancelled, stopping processing", job.getId());
                    break;
                }

                try {
                    processItem(job, item, processedCount, failedCount, repostedEntryIds);
                } catch (Exception e) {
                    log.error("Error processing item {} for job {}: {}", item.getId(), job.getId(), e.getMessage(), e);
                    failedCount.incrementAndGet();
                }

                // Update progress periodically
                if (processedCount.get() % 100 == 0) {
                    job.setProcessedRecords(processedCount.get());
                    job.setFailedRecords(failedCount.get());
                    repostingJobRepository.save(job);
                }
            }

            // Mark entries as reposted
            if (!repostedEntryIds.isEmpty()) {
                stockLedgerEntryRepository.markAsReposted(repostedEntryIds);
            }

            // Finalize job
            job.setStatus(failedCount.get() > 0 ? RepostingJob.RepostingStatus.PARTIALLY_COMPLETED : RepostingJob.RepostingStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setProcessedRecords(processedCount.get());
            job.setFailedRecords(failedCount.get());
            repostingJobRepository.save(job);

            // Publish event
            publishValuationRecalculatedEvent(job, processedCount.get(), failedCount.get());

            log.info("Valuation recalculation completed for job: {}. Processed: {}, Failed: {}", job.getId(), processedCount.get(), failedCount.get());
            return new Output(job, processedCount.get(), failedCount.get());

        } catch (Exception e) {
            log.error("Valuation recalculation failed for job: {}", job.getId(), e);
            job.setStatus(RepostingJob.RepostingStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorMessage(e.getMessage());
            job.setProcessedRecords(processedCount.get());
            job.setFailedRecords(failedCount.get());
            repostingJobRepository.save(job);
            throw e;
        }
    }

    private List<Item> getItemsToProcess(RepostingJob job) {
        if (job.getItemId() != null) {
            return itemRepository.findById(job.getItemId()).map(List::of).orElse(List.of());
        }

        // If no specific item, get all stock items for the company within date range
        // This would need a custom query - for now, we'll use a simple approach
        return itemRepository.findByCompanyIdAndIsStockItemTrue(job.getCompanyId());
    }

    private void processItem(RepostingJob job, Item item, AtomicLong processedCount, AtomicLong failedCount, List<UUID> repostedEntryIds) {
        UUID warehouseId = job.getWarehouseId();
        if (warehouseId == null) {
            // Process all warehouses for this item - would need a query to get distinct warehouses
            // For now, process default warehouse
            warehouseId = item.getDefaultWarehouseId();
            if (warehouseId == null) {
                log.warn("No warehouse specified for item {} and no default warehouse", item.getId());
                return;
            }
        }

        List<StockLedgerEntry> entries = stockLedgerEntryRepository.findAllForValuationRecalc(
                job.getCompanyId(), item.getId(), warehouseId);

        if (entries.isEmpty()) {
            return;
        }

        // Recalculate based on valuation method
        switch (item.getValuationMethod()) {
            case FIFO -> recalculateFifo(entries, repostedEntryIds);
            case MOVING_AVERAGE -> recalculateMovingAverage(entries, repostedEntryIds);
            case STANDARD -> recalculateStandard(entries, item, repostedEntryIds);
        }

        processedCount.addAndGet(entries.size());
    }

    private void recalculateFifo(List<StockLedgerEntry> entries, List<UUID> repostedEntryIds) {
        // FIFO: Process entries chronologically, maintain queue of incoming layers
        List<StockLedgerEntry> incomingLayers = new ArrayList<>();

        for (StockLedgerEntry entry : entries) {
            BigDecimal qtyChange = entry.getQtyChange();

            if (qtyChange.compareTo(BigDecimal.ZERO) > 0) {
                // Incoming - add to layers
                incomingLayers.add(entry);
            } else {
                // Outgoing - consume from earliest layers
                BigDecimal qtyToConsume = qtyChange.abs();
                BigDecimal outgoingRate = BigDecimal.ZERO;
                BigDecimal totalConsumedValue = BigDecimal.ZERO;

                for (StockLedgerEntry layer : incomingLayers) {
                    if (qtyToConsume.compareTo(BigDecimal.ZERO) <= 0) break;

                    BigDecimal availableInLayer = layer.getStockQtyAfter();
                    // Need to track consumed qty per layer - simplified here
                    BigDecimal consumeFromLayer = qtyToConsume.min(availableInLayer);
                    outgoingRate = layer.getValuationRate();
                    totalConsumedValue = totalConsumedValue.add(consumeFromLayer.multiply(outgoingRate));
                    qtyToConsume = qtyToConsume.subtract(consumeFromLayer);
                }

                entry.setOutgoingRate(outgoingRate);
                entry.setStockValue(totalConsumedValue.negate()); // Negative for outgoing
            }

            // Recalculate running totals
            recalculateRunningTotals(entry, entries.indexOf(entry) > 0 ? entries.get(entries.indexOf(entry) - 1) : null);
            repostedEntryIds.add(entry.getId());
        }
    }

    private void recalculateMovingAverage(List<StockLedgerEntry> entries, List<UUID> repostedEntryIds) {
        BigDecimal runningQty = BigDecimal.ZERO;
        BigDecimal runningValue = BigDecimal.ZERO;
        BigDecimal movingAvgRate = BigDecimal.ZERO;

        for (StockLedgerEntry entry : entries) {
            BigDecimal qtyChange = entry.getQtyChange();

            if (qtyChange.compareTo(BigDecimal.ZERO) > 0) {
                // Incoming - update moving average
                BigDecimal incomingValue = qtyChange.multiply(entry.getValuationRate());
                runningQty = runningQty.add(qtyChange);
                runningValue = runningValue.add(incomingValue);

                if (runningQty.compareTo(BigDecimal.ZERO) > 0) {
                    movingAvgRate = runningValue.divide(runningQty, 6, RoundingMode.HALF_UP);
                }
                entry.setIncomingRate(entry.getValuationRate());
                entry.setValuationMethod(Item.ValuationMethod.MOVING_AVERAGE);
            } else {
                // Outgoing - use current moving average
                entry.setOutgoingRate(movingAvgRate);
                BigDecimal outgoingValue = qtyChange.abs().multiply(movingAvgRate);
                entry.setStockValue(outgoingValue.negate());
                runningQty = runningQty.add(qtyChange);
                runningValue = runningValue.add(entry.getStockValue());
            }

            entry.setValuationRate(movingAvgRate);
            recalculateRunningTotals(entry, entries.indexOf(entry) > 0 ? entries.get(entries.indexOf(entry) - 1) : null);
            repostedEntryIds.add(entry.getId());
        }
    }

    private void recalculateStandard(List<StockLedgerEntry> entries, Item item, List<UUID> repostedEntryIds) {
        BigDecimal standardRate = item.getStandardRate();

        for (StockLedgerEntry entry : entries) {
            entry.setValuationRate(standardRate);
            entry.setIncomingRate(standardRate);
            entry.setOutgoingRate(standardRate);
            entry.setStockValue(entry.getQtyChange().multiply(standardRate));
            entry.setValuationMethod(Item.ValuationMethod.STANDARD);
            recalculateRunningTotals(entry, entries.indexOf(entry) > 0 ? entries.get(entries.indexOf(entry) - 1) : null);
            repostedEntryIds.add(entry.getId());
        }
    }

    private void recalculateRunningTotals(StockLedgerEntry entry, StockLedgerEntry previousEntry) {
        BigDecimal prevQty = previousEntry != null ? previousEntry.getStockQtyAfter() : BigDecimal.ZERO;
        BigDecimal prevValue = previousEntry != null ? previousEntry.getStockValueAfter() : BigDecimal.ZERO;

        entry.setStockQtyAfter(prevQty.add(entry.getQtyChange()));
        entry.setStockValueAfter(prevValue.add(entry.getStockValue()));
    }

    private void publishValuationRecalculatedEvent(RepostingJob job, long processedCount, long failedCount) {
        ValuationRecalculatedEvent event = new ValuationRecalculatedEvent(
                job.getId(),
                job.getCompanyId(),
                job.getItemId(),
                job.getWarehouseId(),
                job.getPostingDateFrom(),
                job.getPostingDateTo(),
                processedCount,
                failedCount,
                job.getStatus() == RepostingJob.RepostingStatus.COMPLETED,
                LocalDateTime.now()
        );

        try {
            rabbitTemplate.convertAndSend(inventoryExchange, valuationRecalculatedRoutingKey, event);
            log.debug("Published ValuationRecalculatedEvent for job: {}", job.getId());
        } catch (Exception e) {
            log.error("Failed to publish ValuationRecalculatedEvent for job: {}", job.getId(), e);
        }
    }
}