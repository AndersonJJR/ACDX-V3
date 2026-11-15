package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation;
import com.erp.brazil.manufacturing.domain.model.ProductionOrderOperation.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductionOrderOperationRepository extends JpaRepository<ProductionOrderOperation, UUID> {

    List<ProductionOrderOperation> findByProductionOrderIdOrderBySequence(UUID productionOrderId);

    List<ProductionOrderOperation> findByProductionOrderIdAndStatus(UUID productionOrderId, OperationStatus status);

    List<ProductionOrderOperation> findByWorkCenterId(UUID workCenterId);

    List<ProductionOrderOperation> findByWorkCenterIdAndStatus(UUID workCenterId, OperationStatus status);

    List<ProductionOrderOperation> findByRoutingOperationId(UUID routingOperationId);

    List<ProductionOrderOperation> findBySubcontractingOrderId(UUID subcontractingOrderId);
}