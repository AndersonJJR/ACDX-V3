package com.erp.brazil.manufacturing.application.usecase;

import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.model.BOM.BOMStatus;
import com.erp.brazil.manufacturing.domain.model.BOMItem;
import com.erp.brazil.manufacturing.domain.repository.BOMRepository;
import com.erp.brazil.manufacturing.domain.repository.BOMItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplodeBOMUseCase {

    private final BOMRepository bomRepository;
    private final BOMItemRepository bomItemRepository;

    public record BOMExplosionResult(
            UUID componentId,
            String componentType,
            BigDecimal quantityPerUnit,
            BigDecimal totalQuantity,
            Integer level,
            String path,
            boolean isPhantom,
            UUID bomId,
            Integer position,
            String unitOfMeasure
    ) {}

    public record ExplodeBOMCommand(
            UUID bomId,
            BigDecimal productionQuantity,
            boolean includePhantoms
    ) {}

    @Transactional(readOnly = true)
    public List<BOMExplosionResult> execute(ExplodeBOMCommand command) {
        log.info("Exploding BOM {} with quantity {}", command.bomId(), command.productionQuantity());

        BOM bom = bomRepository.findById(command.bomId())
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + command.bomId()));

        if (bom.getStatus() != BOMStatus.ACTIVE) {
            throw new IllegalStateException("BOM is not active: " + bom.getStatus());
        }

        BigDecimal baseQty = bom.getBaseQuantity() != null ? bom.getBaseQuantity() : BigDecimal.ONE;
        BigDecimal multiplier = command.productionQuantity().divide(baseQty, 6, RoundingMode.HALF_UP);

        List<BOMRepository.BOMExplosionResult> rawResults = bomRepository.explodeBOM(command.bomId());

        List<BOMExplosionResult> results = new ArrayList<>();

        for (BOMRepository.BOMExplosionResult raw : rawResults) {
            if (!command.includePhantoms() && Boolean.TRUE.equals(raw.getIsPhantom())) {
                continue;
            }

            BigDecimal itemQty = raw.getQuantity() != null ? raw.getQuantity() : BigDecimal.ZERO;
            BigDecimal itemScrap = raw.getItemScrap() != null ? raw.getItemScrap() : BigDecimal.ZERO;
            BigDecimal bomScrap = bom.getScrapPercentage() != null ? bom.getScrapPercentage() : BigDecimal.ZERO;

            BigDecimal totalScrapFactor = BigDecimal.ONE
                    .add(bomScrap.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                    .add(itemScrap.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));

            BigDecimal totalQty = itemQty.multiply(multiplier).multiply(totalScrapFactor)
                    .setScale(6, RoundingMode.HALF_UP);

            results.add(new BOMExplosionResult(
                    raw.getComponentId(),
                    raw.getComponentType(),
                    itemQty,
                    totalQty,
                    raw.getLevel(),
                    raw.getPath(),
                    Boolean.TRUE.equals(raw.getIsPhantom()),
                    raw.getId(),
                    raw.getPosition(),
                    raw.getUnitOfMeasure()
            ));
        }

        results.sort(Comparator.comparing(BOMExplosionResult::level)
                .thenComparing(BOMExplosionResult::position));

        log.info("BOM explosion completed: {} components found", results.size());
        return results;
    }

    @Transactional(readOnly = true)
    public List<BOMExplosionResult> explodeByProduct(UUID productId, BigDecimal productionQuantity, boolean includePhantoms) {
        var activeBom = bomRepository.findByProductIdAndStatusOrderByVersionDesc(productId, BOMStatus.ACTIVE);
        if (activeBom.isEmpty()) {
            throw new IllegalArgumentException("No active BOM found for product: " + productId);
        }
        return execute(new ExplodeBOMCommand(activeBom.getFirst().getId(), productionQuantity, includePhantoms));
    }

    @Transactional(readOnly = true)
    public List<BOMExplosionResult> explodeBOMTree(UUID bomId) {
        BOM bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found: " + bomId));

        List<BOMExplosionResult> results = new ArrayList<>();
        explodeRecursive(bom, BigDecimal.ONE, 0, bom.getId().toString(), results);
        return results;
    }

    private void explodeRecursive(BOM bom, BigDecimal parentQty, int level, String path, List<BOMExplosionResult> results) {
        List<BOMItem> items = bomItemRepository.findByBomIdOrderByPosition(bom.getId());

        for (BOMItem item : items) {
            BigDecimal itemQty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
            BigDecimal totalQty = parentQty.multiply(itemQty).setScale(6, RoundingMode.HALF_UP);

            results.add(new BOMExplosionResult(
                    item.getComponentId(),
                    item.getComponentType(),
                    itemQty,
                    totalQty,
                    level + 1,
                    path + "->" + item.getComponentId(),
                    Boolean.TRUE.equals(item.getIsPhantom()),
                    bom.getId(),
                    item.getPosition(),
                    item.getUnitOfMeasure()
            ));

            if (Boolean.TRUE.equals(item.getIsPhantom())) {
                var childBoms = bomRepository.findByProductIdAndStatusOrderByVersionDesc(item.getComponentId(), BOMStatus.ACTIVE);
                childBoms.ifPresentOrElse(
                        childBomList -> {
                            if (!childBomList.isEmpty()) {
                                explodeRecursive(childBomList.getFirst(), totalQty, level + 1, path + "->" + item.getComponentId(), results);
                            }
                        },
                        () -> log.warn("Phantom BOM component {} has no active BOM", item.getComponentId())
                );
            }
        }
    }
}