package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Customer;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import com.erp.brazil.finance.domain.model.InvoiceStatus;
import com.erp.brazil.finance.domain.model.ReceivableInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivableInvoiceRepository extends JpaRepository<ReceivableInvoice, UUID> {

    Optional<ReceivableInvoice> findByCustomerAndInvoiceNumberAndInvoiceSeries(Customer customer, String invoiceNumber, String invoiceSeries);

    List<ReceivableInvoice> findByCustomerOrderByInvoiceDateDesc(Customer customer);

    List<ReceivableInvoice> findByCustomerAndStatus(Customer customer, InvoiceStatus status);

    List<ReceivableInvoice> findByFiscalPeriodOrderByInvoiceDateDesc(FiscalPeriod fiscalPeriod);

    List<ReceivableInvoice> findByFiscalPeriodAndStatus(FiscalPeriod fiscalPeriod, InvoiceStatus status);

    List<ReceivableInvoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<ReceivableInvoice> findByDueDateLessThanEqualAndStatusIn(LocalDate date, List<InvoiceStatus> statuses);

    List<ReceivableInvoice> findByStatusIn(List<InvoiceStatus> statuses);

    Page<ReceivableInvoice> findByStatusIn(List<InvoiceStatus> statuses, Pageable pageable);

    @Query("SELECT r FROM ReceivableInvoice r WHERE r.customer = :customer AND r.balanceAmount > 0 ORDER BY r.dueDate ASC")
    List<ReceivableInvoice> findOpenInvoicesByCustomer(@Param("customer") Customer customer);

    @Query("SELECT r FROM ReceivableInvoice r WHERE r.dueDate <= :date AND r.balanceAmount > 0")
    List<ReceivableInvoice> findOverdueInvoices(@Param("date") LocalDate date);

    Page<ReceivableInvoice> findByCustomer(Customer customer, Pageable pageable);

    Page<ReceivableInvoice> findByFiscalPeriod(FiscalPeriod fiscalPeriod, Pageable pageable);

    Page<ReceivableInvoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<ReceivableInvoice> findAll(Pageable pageable);
}