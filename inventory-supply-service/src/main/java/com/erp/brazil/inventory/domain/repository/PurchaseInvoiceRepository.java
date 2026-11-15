package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.PurchaseInvoice;
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
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, UUID> {

    Optional<PurchaseInvoice> findByCompanyIdAndInvoiceNo(UUID companyId, String invoiceNo);

    Optional<PurchaseInvoice> findByCompanyIdAndNfeAccessKey(UUID companyId, String nfeAccessKey);

    List<PurchaseInvoice> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    Page<PurchaseInvoice> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId, Pageable pageable);

    List<PurchaseInvoice> findByCompanyIdAndStatus(UUID companyId, PurchaseInvoice.PIStatus status);

    List<PurchaseInvoice> findByCompanyIdAndStatusIn(UUID companyId, List<PurchaseInvoice.PIStatus> statuses);

    List<PurchaseInvoice> findByPurchaseOrderId(UUID purchaseOrderId);

    List<PurchaseInvoice> findByPurchaseReceiptId(UUID purchaseReceiptId);

    List<PurchaseInvoice> findByCompanyIdAndInvoiceDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<PurchaseInvoice> findByCompanyIdAndDueDateBeforeAndStatusIn(UUID companyId, LocalDate dueDate, List<PurchaseInvoice.PIStatus> statuses);

    @Query("SELECT pi FROM PurchaseInvoice pi WHERE pi.companyId = :companyId AND pi.status IN ('VALIDATED', 'APPROVED', 'BLOCKED') AND pi.outstandingAmount > 0 ORDER BY pi.dueDate ASC")
    List<PurchaseInvoice> findOutstandingInvoices(@Param("companyId") UUID companyId);

    @Query("SELECT pi FROM PurchaseInvoice pi WHERE pi.companyId = :companyId AND pi.status = 'BLOCKED'")
    List<PurchaseInvoice> findBlockedInvoices(@Param("companyId") UUID companyId);

    boolean existsByCompanyIdAndInvoiceNoAndIdNot(UUID companyId, String invoiceNo, UUID id);

    boolean existsByCompanyIdAndNfeAccessKeyAndIdNot(UUID companyId, String nfeAccessKey, UUID id);

    @Query("SELECT SUM(pi.outstandingAmount) FROM PurchaseInvoice pi WHERE pi.companyId = :companyId AND pi.supplierId = :supplierId AND pi.status IN ('VALIDATED', 'APPROVED', 'BLOCKED', 'PARTIALLY_PAID', 'OVERDUE')")
    java.math.BigDecimal getSupplierOutstandingAmount(@Param("companyId") UUID companyId, @Param("supplierId") UUID supplierId);
}