package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation;
import com.erp.brazil.manufacturing.domain.model.WorkOrderOperation.OperationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderOperationRepository extends JpaRepository<WorkOrderOperation, UUID> {

    List<WorkOrderOperation> findByWorkOrderIdOrderBySequence(UUID workOrderId);

    List<WorkOrderOperation> findByWorkOrderIdAndStatus(UUID workOrderId, OperationStatus status);

    List<WorkOrderOperation> findByWorkCenterId(UUID workCenterId);

    List<WorkOrderOperation> findByWorkStationId(UUID workStationId);
}