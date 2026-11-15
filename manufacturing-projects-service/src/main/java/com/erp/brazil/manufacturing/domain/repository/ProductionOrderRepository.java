package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.ProductionOrder;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.OrderType;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.Priority;
import com.erp.brazil.manufacturing.domain.model.ProductionOrder.ProductionOrderStatus;
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
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, UUID> {

    Optional<ProductionOrder> findByOrderNumber(String orderNumber);

    List<ProductionOrder> findByProductId(UUID productId);

    List<ProductionOrder> findByStatus(ProductionOrderStatus status);

    List<ProductionOrder> findByStatusIn(List<ProductionOrderStatus> statuses);

    List<ProductionOrder> findByOrderType(OrderType orderType);

    List<ProductionOrder> findBySalesOrderId(UUID salesOrderId);

    List<ProductionOrder> findByProjectId(UUID projectId);

    Page<ProductionOrder> findByStatus(ProductionOrderStatus status, Pageable pageable);

    Page<ProductionOrder> findByProductId(UUID productId, Pageable pageable);

    Page<ProductionOrder> findBySalesOrderId(UUID salesOrderId, Pageable pageable);

    @Query("SELECT po FROM ProductionOrder po WHERE po.plannedStartDate BETWEEN :start AND :end")
    List<ProductionOrder> findByPlannedStartDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT po FROM ProductionOrder po WHERE po.status IN :statuses AND po.plannedStartDate <= :now ORDER BY po.priority DESC, po.plannedStartDate ASC")
    List<ProductionOrder> findReleasableOrders(@Param("statuses") List<ProductionOrderStatus> statuses, @Param("now") LocalDateTime now);

    List<ProductionOrder> findByPriorityAndStatus(Priority priority, ProductionOrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(ProductionOrderStatus status);
}