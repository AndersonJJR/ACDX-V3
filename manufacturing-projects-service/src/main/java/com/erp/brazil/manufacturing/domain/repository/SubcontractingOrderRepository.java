package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder;
import com.erp.brazil.manufacturing.domain.model.SubcontractingOrder.SubcontractingStatus;
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
public interface SubcontractingOrderRepository extends JpaRepository<SubcontractingOrder, UUID> {

    Optional<SubcontractingOrder> findByOrderNumber(String orderNumber);

    List<SubcontractingOrder> findByVendorId(UUID vendorId);

    List<SubcontractingOrder> findByProductionOrderId(UUID productionOrderId);

    List<SubcontractingOrder> findByStatus(SubcontractingStatus status);

    Page<SubcontractingOrder> findByStatus(SubcontractingStatus status, Pageable pageable);

    Page<SubcontractingOrder> findByVendorId(UUID vendorId, Pageable pageable);

    @Query("SELECT so FROM SubcontractingOrder so WHERE so.plannedEndDate < :now AND so.status IN ('SENT', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<SubcontractingOrder> findOverdueOrders(@Param("now") LocalDateTime now);

    @Query("SELECT so FROM SubcontractingOrder so WHERE so.productionOrderOperationId = :prodOpId")
    List<SubcontractingOrder> findByProductionOrderOperationId(@Param("prodOpId") UUID prodOpId);

    boolean existsByOrderNumber(String orderNumber);
}