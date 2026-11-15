package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.PurchaseReceipt;
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
public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, UUID> {

    Optional<PurchaseReceipt> findByCompanyIdAndReceiptNo(UUID companyId, String receiptNo);

    List<PurchaseReceipt> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    Page<PurchaseReceipt> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId, Pageable pageable);

    List<PurchaseReceipt> findByCompanyIdAndStatus(UUID companyId, PurchaseReceipt.PRStatus status);

    List<PurchaseReceipt> findByPurchaseOrderId(UUID purchaseOrderId);

    List<PurchaseReceipt> findByCompanyIdAndReceiptDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<PurchaseReceipt> findByCompanyIdAndStatusIn(UUID companyId, List<PurchaseReceipt.PRStatus> statuses);

    @Query("SELECT pr FROM PurchaseReceipt pr WHERE pr.companyId = :companyId AND pr.purchaseOrderId = :poId AND pr.status IN ('ACCEPTED', 'PARTIALLY_RECEIVED', 'COMPLETED')")
    List<PurchaseReceipt> findCompletedReceiptsForPO(@Param("companyId") UUID companyId, @Param("poId") UUID poId);

    @Query("SELECT SUM(pr.totalQty) FROM PurchaseReceipt pr WHERE pr.purchaseOrderId = :poId AND pr.status IN ('ACCEPTED', 'PARTIALLY_RECEIVED', 'COMPLETED')")
    java.math.BigDecimal getTotalReceivedQtyForPO(@Param("poId") UUID poId);

    boolean existsByCompanyIdAndReceiptNoAndIdNot(UUID companyId, String receiptNo, UUID id);

    @Query("SELECT pr FROM PurchaseReceipt pr WHERE pr.companyId = :companyId AND pr.status = 'QUALITY_INSPECTION'")
    List<PurchaseReceipt> findPendingInspection(@Param("companyId") UUID companyId);
}