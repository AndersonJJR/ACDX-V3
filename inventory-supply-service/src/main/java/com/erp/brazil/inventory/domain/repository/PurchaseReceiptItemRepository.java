package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.PurchaseReceiptItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseReceiptItemRepository extends JpaRepository<PurchaseReceiptItem, UUID> {

    List<PurchaseReceiptItem> findByPurchaseReceiptId(UUID purchaseReceiptId);

    Page<PurchaseReceiptItem> findByPurchaseReceiptId(UUID purchaseReceiptId, Pageable pageable);

    List<PurchaseReceiptItem> findByPurchaseOrderItemId(UUID purchaseOrderItemId);

    List<PurchaseReceiptItem> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    @Query("SELECT pri FROM PurchaseReceiptItem pri WHERE pri.purchaseReceiptId = :receiptId AND pri.qty > pri.rejectedQty")
    List<PurchaseReceiptItem> findAcceptedItems(@Param("receiptId") UUID receiptId);

    @Query("SELECT SUM(pri.qty - pri.rejectedQty) FROM PurchaseReceiptItem pri WHERE pri.purchaseOrderItemId = :poItemId")
    java.math.BigDecimal getTotalAcceptedQtyForPOItem(@Param("poItemId") UUID poItemId);

    @Query("SELECT pri FROM PurchaseReceiptItem pri WHERE pri.companyId = :companyId AND pri.itemId = :itemId AND pri.batchNo IS NOT NULL AND pri.batchNo != ''")
    List<PurchaseReceiptItem> findBatchedItemsByItem(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId);
}