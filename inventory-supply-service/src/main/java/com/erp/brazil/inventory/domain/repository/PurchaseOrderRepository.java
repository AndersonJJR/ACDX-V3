package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    Optional<PurchaseOrder> findByCompanyIdAndPoNo(UUID companyId, String poNo);

    List<PurchaseOrder> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    Page<PurchaseOrder> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId, Pageable pageable);

    List<PurchaseOrder> findByCompanyIdAndStatus(UUID companyId, PurchaseOrder.POStatus status);

    Page<PurchaseOrder> findByCompanyIdAndStatus(UUID companyId, PurchaseOrder.POStatus status, Pageable pageable);

    List<PurchaseOrder> findByCompanyIdAndPoDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<PurchaseOrder> findByCompanyIdAndStatusIn(UUID companyId, List<PurchaseOrder.POStatus> statuses);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.companyId = :companyId AND po.status IN ('APPROVED', 'PARTIALLY_RECEIVED') ORDER BY po.expectedDeliveryDate ASC")
    List<PurchaseOrder> findOpenPurchaseOrders(@Param("companyId") UUID companyId);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.companyId = :companyId AND po.status = 'APPROVED' AND po.expectedDeliveryDate < :today")
    List<PurchaseOrder> findOverduePurchaseOrders(@Param("companyId") UUID companyId, @Param("today") LocalDate today);

    boolean existsByCompanyIdAndPoNoAndIdNot(UUID companyId, String poNo, UUID id);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.companyId = :companyId AND po.status = 'APPROVED' AND po.expectedDeliveryDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findByExpectedDeliveryDateRange(@Param("companyId") UUID companyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}