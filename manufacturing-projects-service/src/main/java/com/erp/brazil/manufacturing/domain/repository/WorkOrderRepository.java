package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.Priority;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    Optional<WorkOrder> findByOrderNumber(String orderNumber);

    List<WorkOrder> findByProductionOrderId(UUID productionOrderId);

    List<WorkOrder> findByProductionOrderOperationId(UUID productionOrderOperationId);

    List<WorkOrder> findByWorkCenterId(UUID workCenterId);

    List<WorkOrder> findByWorkStationId(UUID workStationId);

    List<WorkOrder> findByStatus(WorkOrderStatus status);

    List<WorkOrder> findByStatusIn(List<WorkOrderStatus> statuses);

    List<WorkOrder> findByAssignedTo(String assignedTo);

    Page<WorkOrder> findByWorkCenterIdAndStatus(UUID workCenterId, WorkOrderStatus status, Pageable pageable);

    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.plannedStartDate BETWEEN :start AND :end")
    List<WorkOrder> findByPlannedStartDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status IN :statuses AND wo.plannedStartDate <= :now ORDER BY wo.priority DESC, wo.plannedStartDate ASC")
    List<WorkOrder> findDispatchableOrders(@Param("statuses") List<WorkOrderStatus> statuses, @Param("now") LocalDateTime now);

    List<WorkOrder> findByPriorityAndStatus(Priority priority, WorkOrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

    long countByWorkCenterIdAndStatus(UUID workCenterId, WorkOrderStatus status);
}