package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesOrder;
import com.erp.brazil.sales.domain.model.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    Optional<SalesOrder> findByNumberAndCompanyId(String number, UUID companyId);

    List<SalesOrder> findByCompanyIdAndStatus(UUID companyId, SalesOrderStatus status);

    List<SalesOrder> findByCustomerId(UUID customerId);

    List<SalesOrder> findByOpportunityId(UUID opportunityId);

    Optional<SalesOrder> findByQuotationId(UUID quotationId);

    @Query("SELECT so FROM SalesOrder so WHERE so.companyId = :companyId AND so.status IN (:statuses) ORDER BY so.orderDate DESC")
    List<SalesOrder> findByCompanyIdAndStatusIn(@Param("companyId") UUID companyId, @Param("statuses") List<SalesOrderStatus> statuses);

    @Query("SELECT so FROM SalesOrder so WHERE so.companyId = :companyId AND so.status = :status AND so.deliveryDate < :today ORDER BY so.deliveryDate")
    List<SalesOrder> findOverdueDeliveries(@Param("companyId") UUID companyId, @Param("status") SalesOrderStatus status, @Param("today") LocalDate today);

    @Query("SELECT so FROM SalesOrder so WHERE so.companyId = :companyId AND so.salesPerson.id = :salesPersonId AND so.status IN (:statuses) ORDER BY so.orderDate DESC")
    List<SalesOrder> findBySalesPersonAndStatuses(@Param("companyId") UUID companyId, @Param("salesPersonId") UUID salesPersonId, @Param("statuses") List<SalesOrderStatus> statuses);

    @Query("SELECT so FROM SalesOrder so WHERE so.companyId = :companyId ORDER BY so.orderDate DESC")
    Page<SalesOrder> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(so) FROM SalesOrder so WHERE so.companyId = :companyId AND so.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") SalesOrderStatus status);

    @Query("SELECT SUM(so.totalAmount) FROM SalesOrder so WHERE so.companyId = :companyId AND so.status IN (:statuses) AND so.orderDate BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalAmountByPeriod(@Param("companyId") UUID companyId, @Param("statuses") List<SalesOrderStatus> statuses, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT so FROM SalesOrder so WHERE so.companyId = :companyId AND so.status = :status AND so.confirmedAt IS NOT NULL ORDER BY so.confirmedAt")
    List<SalesOrder> findConfirmedAwaitingReservation(@Param("companyId") UUID companyId, @Param("status") SalesOrderStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE SalesOrder so SET so.status = :status WHERE so.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") SalesOrderStatus status);

    boolean existsByNumberAndCompanyIdAndIdNot(String number, UUID companyId, UUID id);
}