package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.PurchaseInvoiceItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseInvoiceItemRepository extends JpaRepository<PurchaseInvoiceItem, UUID> {

    List<PurchaseInvoiceItem> findByPurchaseInvoiceId(UUID purchaseInvoiceId);

    Page<PurchaseInvoiceItem> findByPurchaseInvoiceId(UUID purchaseInvoiceId, Pageable pageable);

    List<PurchaseInvoiceItem> findByPurchaseOrderItemId(UUID purchaseOrderItemId);

    List<PurchaseInvoiceItem> findByPurchaseReceiptItemId(UUID purchaseReceiptItemId);

    List<PurchaseInvoiceItem> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    @Query("SELECT pii FROM PurchaseInvoiceItem pii WHERE pii.purchaseInvoiceId = :invoiceId AND pii.matchStatus != 'MATCHED'")
    List<PurchaseInvoiceItem> findUnmatchedItems(@Param("invoiceId") UUID invoiceId);

    @Query("SELECT pii FROM PurchaseInvoiceItem pii WHERE pii.purchaseInvoiceId = :invoiceId AND pii.matchStatus IN ('QUANTITY_MISMATCH', 'RATE_MISMATCH', 'BOTH_MISMATCH', 'OVER_BILLED')")
    List<PurchaseInvoiceItem> findMismatchedItems(@Param("invoiceId") UUID invoiceId);

    @Query("SELECT SUM(pii.varianceAmount) FROM PurchaseInvoiceItem pii WHERE pii.purchaseInvoiceId = :invoiceId")
    java.math.BigDecimal getTotalVarianceAmount(@Param("invoiceId") UUID invoiceId);
}