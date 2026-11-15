package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Customer;
import com.erp.brazil.finance.domain.model.ReceivableInvoice;
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
public interface ReceivableInvoiceRepository extends JpaRepository<ReceivableInvoice, UUID> {

    Optional<ReceivableInvoice> findByCustomerAndNumberAndSeries(Customer customer, String number, String series);

    Optional<ReceivableInvoice> findByAccessKey(String accessKey);

    List<ReceivableInvoice> findByCustomerOrderByIssueDateDesc(Customer customer);

    List<ReceivableInvoice> findByCustomerAndStatusOrderByDueDateAsc(Customer customer, InvoiceStatus status);

    List<ReceivableInvoice> findByCompanyIdAndStatusOrderByDueDateAsc(UUID companyId, InvoiceStatus status);

    List<ReceivableInvoice> findByCompanyIdAndDueDateBetweenOrderByDueDateAsc(UUID companyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ri FROM ReceivableInvoice ri WHERE ri.companyId = :companyId AND ri.dueDate <= :date AND ri.status IN ('PENDING', 'AUTHORIZED', 'PARTIALLY_PAID', 'OVERDUE') ORDER BY ri.dueDate ASC")
    List<ReceivableInvoice> findOverdueAndDueReceivables(@Param("companyId") UUID companyId, @Param("date") LocalDate date);

    @Query("SELECT ri FROM ReceivableInvoice ri WHERE ri.companyId = :companyId AND ri.status IN ('PENDING', 'AUTHORIZED', 'PARTIALLY_PAID') ORDER BY ri.dueDate ASC")
    List<ReceivableInvoice> findOpenReceivables(@Param("companyId") UUID companyId);

    Page<ReceivableInvoice> findByCustomer(Customer customer, Pageable pageable);

    Page<ReceivableInvoice> findByCompanyIdAndStatus(UUID companyId, InvoiceStatus status, Pageable pageable);

    Page<ReceivableInvoice> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCustomerAndNumberAndSeries(Customer customer, String number, String series);

    boolean existsByAccessKey(String accessKey);
}