package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.CommissionPayment;
import com.erp.brazil.sales.domain.model.CommissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommissionPaymentRepository extends JpaRepository<CommissionPayment, UUID> {

    List<CommissionPayment> findByCompanyIdAndStatus(UUID companyId, CommissionStatus status);

    List<CommissionPayment> findBySalesPersonId(UUID salesPersonId);

    List<CommissionPayment> findByCommissionRuleId(UUID commissionRuleId);

    List<CommissionPayment> findBySalesOrderId(UUID salesOrderId);

    List<CommissionPayment> findBySalesInvoiceId(UUID salesInvoiceId);

    @Query("SELECT cp FROM CommissionPayment cp WHERE cp.companyId = :companyId AND cp.periodStart >= :start AND cp.periodEnd <= :end ORDER BY cp.periodStart")
    List<CommissionPayment> findByCompanyIdAndPeriod(@Param("companyId") UUID companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT cp FROM CommissionPayment cp WHERE cp.companyId = :companyId AND cp.salesPerson.id = :salesPersonId AND cp.periodStart >= :start AND cp.periodEnd <= :end ORDER BY cp.periodStart")
    List<CommissionPayment> findBySalesPersonAndPeriod(@Param("companyId") UUID companyId, @Param("salesPersonId") UUID salesPersonId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(cp.amount) FROM CommissionPayment cp WHERE cp.companyId = :companyId AND cp.status = :status AND cp.periodStart >= :start AND cp.periodEnd <= :end")
    BigDecimal sumByCompanyIdAndStatusAndPeriod(@Param("companyId") UUID companyId, @Param("status") CommissionStatus status, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT cp FROM CommissionPayment cp WHERE cp.companyId = :companyId ORDER BY cp.periodStart DESC")
    Page<CommissionPayment> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    boolean existsBySalesPersonIdAndPeriodStartAndPeriodEndAndCommissionRuleId(
            UUID salesPersonId, LocalDate periodStart, LocalDate periodEnd, UUID commissionRuleId);
}