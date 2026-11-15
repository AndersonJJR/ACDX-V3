package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.WorkOrder;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.WorkOrderStatus;
import com.erp.brazil.manufacturing.domain.model.WorkOrder.Priority;
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

    List<WorkOrder> findByWorkCenterIdAndStatus(UUID workCenterId, WorkOrderStatus status);

    List<WorkOrder> findByStatus(WorkOrderStatus status);

    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);

    Page<WorkOrder> findByWorkCenterId(UUID workCenterId, Pageable pageable);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.workCenterId = :workCenterId AND wo.plannedStartDate BETWEEN :startDate AND :endDate ORDER BY wo.plannedStartDate ASC")
    List<WorkOrder> findByWorkCenterAndDateRange(@Param("workCenterId") UUID workCenterId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT wo FROM WorkOrder wo WHERE wo.assignedTo = :assignedTo AND wo.status IN ('RELEASED', 'IN_PROGRESS') ORDER BY wo.plannedStartDate ASC")
    List<WorkOrder> findByAssigneeAndActiveStatus(@Param("assignedTo") String assignedTo);

    boolean existsByOrderNumber(String orderNumber);

    long countByWorkCenterIdAndStatus(UUID workCenterId, WorkOrderStatus status);
}