package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderOperationRepository extends JpaRepository<WorkOrderOperation, UUID> {

    List<WorkOrderOperation> findByWorkOrderIdOrderBySequence(UUID workOrderId);

    List<WorkOrderOperation> findByWorkOrderIdAndStatus(UUID workOrderId, OperationStatus status);

    @Query("SELECT woo FROM WorkOrderOperation woo WHERE woo.workOrder.id = :workOrderId AND woo.status = 'PENDING' ORDER BY woo.sequence ASC")
    List<WorkOrderOperation> findNextPendingOperation(@Param("workOrderId") UUID workOrderId);

    @Query("SELECT woo FROM WorkOrderOperation woo WHERE woo.workOrder.id = :workOrderId AND woo.status IN ('IN_PROGRESS', 'ON_HOLD')")
    List<WorkOrderOperation> findActiveOperations(@Param("workOrderId") UUID workOrderId);

    boolean existsByWorkOrderIdAndSequence(UUID workOrderId, Integer sequence);
}