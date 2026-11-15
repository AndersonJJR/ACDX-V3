package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesInvoice;
import com.erp.brazil.sales.domain.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, UUID> {

    Optional<SalesInvoice> findByNumberAndCompanyId(String number, UUID companyId);

    Optional<SalesInvoice> findByAccessKey(String accessKey);

    List<SalesInvoice> findByCompanyIdAndStatus(UUID companyId, InvoiceStatus status);

    List<SalesInvoice> findByCustomerId(UUID customerId);

    List<SalesInvoice> findBySalesOrderId(UUID salesOrderId);

    List<SalesInvoice> findByDeliveryNoteId(UUID deliveryNoteId);

    @Query("SELECT si FROM SalesInvoice si WHERE si.companyId = :companyId AND si.status IN (:statuses) ORDER BY si.issueDate DESC")
    List<SalesInvoice> findByCompanyIdAndStatusIn(@Param("companyId") UUID companyId, @Param("statuses") List<InvoiceStatus> statuses);

    @Query("SELECT si FROM SalesInvoice si WHERE si.companyId = :companyId AND si.dueDate < :today AND si.status NOT IN (:paidStatuses) ORDER BY si.dueDate")
    List<SalesInvoice> findOverdueInvoices(@Param("companyId") UUID companyId, @Param("today") LocalDate today, @Param("paidStatuses") List<InvoiceStatus> paidStatuses);

    @Query("SELECT si FROM SalesInvoice si WHERE si.companyId = :companyId AND si.issueDate BETWEEN :start AND :end ORDER BY si.issueDate")
    List<SalesInvoice> findByIssueDateBetween(@Param("companyId") UUID companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(si.totalAmount) FROM SalesInvoice si WHERE si.companyId = :companyId AND si.status IN (:statuses) AND si.issueDate BETWEEN :start AND :end")
    BigDecimal sumTotalAmountByPeriod(@Param("companyId") UUID companyId, @Param("statuses") List<InvoiceStatus> statuses, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT si FROM SalesInvoice si WHERE si.companyId = :companyId ORDER BY si.issueDate DESC")
    Page<SalesInvoice> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(si) FROM SalesInvoice si WHERE si.companyId = :companyId AND si.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") InvoiceStatus status);

    boolean existsByNumberAndCompanyIdAndIdNot(String number, UUID companyId, UUID id);

    boolean existsByAccessKeyAndIdNot(String accessKey, UUID id);
}