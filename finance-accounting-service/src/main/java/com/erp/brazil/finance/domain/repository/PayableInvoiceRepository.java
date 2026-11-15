package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.PayableInvoice;
import com.erp.brazil.finance.domain.model.Vendor;
import com.erp.brazil.finance.domain.model.InvoiceStatus;
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
public interface PayableInvoiceRepository extends JpaRepository<PayableInvoice, UUID> {

    Optional<PayableInvoice> findByVendorAndNumberAndSeries(Vendor vendor, String number, String series);

    Optional<PayableInvoice> findByAccessKey(String accessKey);

    List<PayableInvoice> findByVendorOrderByIssueDateDesc(Vendor vendor);

    List<PayableInvoice> findByVendorAndStatusOrderByDueDateAsc(Vendor vendor, InvoiceStatus status);

    List<PayableInvoice> findByCompanyIdAndStatusOrderByDueDateAsc(UUID companyId, InvoiceStatus status);

    List<PayableInvoice> findByCompanyIdAndDueDateBetweenOrderByDueDateAsc(UUID companyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT pi FROM PayableInvoice pi WHERE pi.companyId = :companyId AND pi.dueDate <= :date AND pi.status IN ('PENDING', 'AUTHORIZED', 'PARTIALLY_PAID', 'OVERDUE') ORDER BY pi.dueDate ASC")
    List<PayableInvoice> findOverdueAndDuePayables(@Param("companyId") UUID companyId, @Param("date") LocalDate date);

    @Query("SELECT pi FROM PayableInvoice pi WHERE pi.companyId = :companyId AND pi.status IN ('PENDING', 'AUTHORIZED', 'PARTIALLY_PAID') ORDER BY pi.dueDate ASC")
    List<PayableInvoice> findOpenPayables(@Param("companyId") UUID companyId);

    Page<PayableInvoice> findByVendor(Vendor vendor, Pageable pageable);

    Page<PayableInvoice> findByCompanyIdAndStatus(UUID companyId, InvoiceStatus status, Pageable pageable);

    Page<PayableInvoice> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByVendorAndNumberAndSeries(Vendor vendor, String number, String series);

    boolean existsByAccessKey(String accessKey);
}