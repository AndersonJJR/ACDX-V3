package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductionOrderItemRepository extends JpaRepository<ProductionOrderItem, UUID> {

    List<ProductionOrderItem> findByProductionOrderId(UUID productionOrderId);

    List<ProductionOrderItem> findByProductionOrderIdAndStatus(UUID productionOrderId, ItemStatus status);

    List<ProductionOrderItem> findByComponentId(UUID componentId);

    @Query("SELECT poi FROM ProductionOrderItem poi WHERE poi.productionOrder.id = :prodOrderId AND poi.requiredQuantity > poi.issuedQuantity")
    List<ProductionOrderItem> findWithRemainingQuantity(@Param("prodOrderId") UUID prodOrderId);

    boolean existsByProductionOrderIdAndComponentId(UUID productionOrderId, UUID componentId);
}