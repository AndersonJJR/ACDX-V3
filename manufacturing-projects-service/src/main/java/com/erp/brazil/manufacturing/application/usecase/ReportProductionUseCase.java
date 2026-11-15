package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.application.event.ProductionOrderEventPublisher;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.ProductionOrderStatus;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderItemRepository;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderOperationRepository;
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
public class ReportProductionUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderItemRepository productionOrderItemRepository;
    private final ProductionOrderOperationRepository productionOrderOperationRepository;
    private final ProductionOrderEventPublisher eventPublisher;

    public record ReportProductionCommand(
            UUID productionOrderId,
            BigDecimal producedQuantity,
            BigDecimal scrappedQuantity,
            String notes,
            String reportedBy
    ) {}

    public record ReportProductionResult(
            UUID productionOrderId,
            String orderNumber,
            BigDecimal totalProduced,
            BigDecimal totalScrapped,
            ProductionOrderStatus status,
            boolean completed
    ) {}

    @Transactional
    public ReportProductionResult execute(ReportProductionCommand command) {
        log.info("Reporting production for order {}", command.productionOrderId());

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + command.productionOrderId()));

        if (!productionOrder.canBeStarted() && productionOrder.getStatus() != ProductionOrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Production order cannot report production. Current status: " + productionOrder.getStatus());
        }

        BigDecimal produced = command.producedQuantity() != null ? command.producedQuantity() : BigDecimal.ZERO;
        BigDecimal scrapped = command.scrappedQuantity() != null ? command.scrappedQuantity() : BigDecimal.ZERO;

        productionOrder.setProducedQuantity(productionOrder.getProducedQuantity().add(produced));
        productionOrder.setScrappedQuantity(productionOrder.getScrappedQuantity().add(scrapped));
        productionOrder.setActualEndDate(LocalDateTime.now());
        productionOrder.setNotes(command.notes());
        productionOrder.setUpdatedBy(command.reportedBy());

        if (productionOrder.getProducedQuantity().compareTo(productionOrder.getOrderedQuantity()) >= 0) {
            productionOrder.setStatus(ProductionOrderStatus.COMPLETED);
            productionOrder.setCompletedDate(LocalDateTime.now());
        } else {
            productionOrder.setStatus(ProductionOrderStatus.IN_PROGRESS);
        }

        List<ProductionOrderItem> items = productionOrderItemRepository.findByProductionOrderId(command.productionOrderId());
        for (ProductionOrderItem item : items) {
            if (item.getStatus() == ProductionOrderItem.ItemStatus.ISSUED || item.getStatus() == ProductionOrderItem.ItemStatus.PARTIAL) {
                item.setConsumedQuantity(item.getConsumedQuantity().add(produced.multiply(item.getRequiredQuantity())
                        .divide(productionOrder.getOrderedQuantity(), 6, BigDecimal.ROUND_HALF_UP)));
                if (item.getConsumedQuantity().compareTo(item.getRequiredQuantity()) >= 0) {
                    item.setStatus(ProductionOrderItem.ItemStatus.CONSUMED);
                }
            }
        }

        List<ProductionOrderOperation> operations = productionOrderOperationRepository.findByProductionOrderIdOrderBySequence(command.productionOrderId());
        for (ProductionOrderOperation op : operations) {
            if (op.getStatus() == ProductionOrderOperation.OperationStatus.IN_PROGRESS) {
                op.setStatus(ProductionOrderOperation.OperationStatus.COMPLETED);
                op.setActualEndDate(LocalDateTime.now());
            }
        }

        productionOrderRepository.save(productionOrder);

        eventPublisher.publishProductionReported(productionOrder, produced, scrapped);
        if (productionOrder.getStatus() == ProductionOrderStatus.COMPLETED) {
            eventPublisher.publishProductionOrderCompleted(productionOrder);
        }

        log.info("Production reported for order {}: produced={}, scrapped={}", productionOrder.getOrderNumber(), produced, scrapped);
        return new ReportProductionResult(
                productionOrder.getId(),
                productionOrder.getOrderNumber(),
                productionOrder.getProducedQuantity(),
                productionOrder.getScrappedQuantity(),
                productionOrder.getStatus(),
                productionOrder.getStatus() == ProductionOrderStatus.COMPLETED
        );
    }
}