package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.InvoiceStatus;
import com.erp.brazil.finance.domain.model.PayableInvoice;
import com.erp.brazil.finance.domain.model.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayableInvoiceRepository extends JpaRepository<PayableInvoice, UUID> {

    Optional<PayableInvoice> findByVendorAndInvoiceNumberAndInvoiceSeries(Vendor vendor, String invoiceNumber, String invoiceSeries);

    List<PayableInvoice> findByVendorOrderByInvoiceDateDesc(Vendor vendor);

    List<PayableInvoice> findByVendorAndStatus(Vendor vendor, InvoiceStatus status);

    List<PayableInvoice> findByFiscalPeriodOrderByInvoiceDateDesc(FiscalPeriod fiscalPeriod);

    List<PayableInvoice> findByFiscalPeriodAndStatus(FiscalPeriod fiscalPeriod, InvoiceStatus status);

    List<PayableInvoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<PayableInvoice> findByDueDateLessThanEqualAndStatusIn(LocalDate date, List<InvoiceStatus> statuses);

    List<PayableInvoice> findByStatusIn(List<InvoiceStatus> statuses);

    Page<PayableInvoice> findByStatusIn(List<InvoiceStatus> statuses, Pageable pageable);

    @Query("SELECT p FROM PayableInvoice p WHERE p.vendor = :vendor AND p.balanceAmount > 0 ORDER BY p.dueDate ASC")
    List<PayableInvoice> findOpenInvoicesByVendor(@Param("vendor") Vendor vendor);

    @Query("SELECT p FROM PayableInvoice p WHERE p.dueDate <= :date AND p.balanceAmount > 0")
    List<PayableInvoice> findOverdueInvoices(@Param("date") LocalDate date);

    Page<PayableInvoice> findByVendor(Vendor vendor, Pageable pageable);

    Page<PayableInvoice> findByFiscalPeriod(FiscalPeriod fiscalPeriod, Pageable pageable);

    Page<PayableInvoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<PayableInvoice> findAll(Pageable pageable);
}