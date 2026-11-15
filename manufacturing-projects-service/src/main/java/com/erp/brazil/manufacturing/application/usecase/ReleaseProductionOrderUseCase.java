package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.application.event.ProductionOrderEventPublisher;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.ProductionOrderStatus;
import com.erp.brazil.manufacturing.domain.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReleaseProductionOrderUseCase {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductionOrderEventPublisher eventPublisher;

    public record ReleaseProductionOrderCommand(
            UUID productionOrderId,
            String releasedBy
    ) {}

    public record ReleaseProductionOrderResult(
            UUID productionOrderId,
            String orderNumber,
            ProductionOrderStatus newStatus
    ) {}

    @Transactional
    public ReleaseProductionOrderResult execute(ReleaseProductionOrderCommand command) {
        log.info("Releasing production order {}", command.productionOrderId());

        ProductionOrder productionOrder = productionOrderRepository.findById(command.productionOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Production order not found: " + command.productionOrderId()));

        if (!productionOrder.canBeReleased()) {
            throw new IllegalStateException("Production order cannot be released. Current status: " + productionOrder.getStatus());
        }

        productionOrder.setStatus(ProductionOrderStatus.RELEASED);
        productionOrder.setReleasedDate(LocalDateTime.now());
        productionOrder.setUpdatedBy(command.releasedBy());

        productionOrderRepository.save(productionOrder);

        eventPublisher.publishProductionOrderReleased(productionOrder);

        log.info("Production order {} released", productionOrder.getOrderNumber());
        return new ReleaseProductionOrderResult(
                productionOrder.getId(),
                productionOrder.getOrderNumber(),
                productionOrder.getStatus()
        );
    }
}