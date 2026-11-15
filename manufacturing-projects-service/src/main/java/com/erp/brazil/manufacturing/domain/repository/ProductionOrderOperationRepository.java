package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductionOrderOperationRepository extends JpaRepository<ProductionOrderOperation, UUID> {

    List<ProductionOrderOperation> findByProductionOrderIdOrderBySequence(UUID productionOrderId);

    List<ProductionOrderOperation> findByProductionOrderIdAndStatus(UUID productionOrderId, OperationStatus status);

    List<ProductionOrderOperation> findByWorkCenterId(UUID workCenterId);

    List<ProductionOrderOperation> findByWorkCenterIdAndStatus(UUID workCenterId, OperationStatus status);

    @Query("SELECT poo FROM ProductionOrderOperation poo WHERE poo.productionOrder.id = :prodOrderId AND poo.status = 'PENDING' ORDER BY poo.sequence ASC")
    List<ProductionOrderOperation> findNextPendingOperation(@Param("prodOrderId") UUID prodOrderId);

    @Query("SELECT poo FROM ProductionOrderOperation poo WHERE poo.productionOrder.id = :prodOrderId AND poo.status IN ('IN_PROGRESS', 'WAITING_MATERIAL', 'WAITING_CAPACITY', 'READY')")
    List<ProductionOrderOperation> findActiveOperations(@Param("prodOrderId") UUID prodOrderId);

    boolean existsByProductionOrderIdAndSequence(UUID productionOrderId, Integer sequence);
}