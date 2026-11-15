package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderItem.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductionOrderItemRepository extends JpaRepository<ProductionOrderItem, UUID> {

    List<ProductionOrderItem> findByProductionOrderId(UUID productionOrderId);

    List<ProductionOrderItem> findByProductionOrderIdAndStatus(UUID productionOrderId, ItemStatus status);

    List<ProductionOrderItem> findByComponentId(UUID componentId);

    List<ProductionOrderItem> findByBomItemId(UUID bomItemId);

    List<ProductionOrderItem> findByWarehouseId(UUID warehouseId);
}