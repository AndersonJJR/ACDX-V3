package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.PurchaseOrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {

    List<PurchaseOrderItem> findByPurchaseOrderId(UUID purchaseOrderId);

    Page<PurchaseOrderItem> findByPurchaseOrderId(UUID purchaseOrderId, Pageable pageable);

    List<PurchaseOrderItem> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.purchaseOrderId = :poId AND poi.qty > poi.receivedQty")
    List<PurchaseOrderItem> findPendingReceiptItems(@Param("poId") UUID poId);

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.purchaseOrderId = :poId AND poi.qty > poi.billedQty")
    List<PurchaseOrderItem> findPendingBillingItems(@Param("poId") UUID poId);

    @Query("SELECT SUM(poi.qty - poi.receivedQty) FROM PurchaseOrderItem poi WHERE poi.purchaseOrderId = :poId")
    java.math.BigDecimal getTotalPendingQty(@Param("poId") UUID poId);

    @Query("SELECT SUM(poi.qty - poi.billedQty) FROM PurchaseOrderItem poi WHERE poi.purchaseOrderId = :poId")
    java.math.BigDecimal getTotalPendingBillingQty(@Param("poId") UUID poId);
}