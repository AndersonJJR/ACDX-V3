package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Quotation;
import com.erp.brazil.sales.domain.model.QuotationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    Optional<Quotation> findByNumberAndCompanyId(String number, UUID companyId);

    List<Quotation> findByCompanyIdAndStatus(UUID companyId, QuotationStatus status);

    List<Quotation> findByCustomerId(UUID customerId);

    List<Quotation> findByOpportunityId(UUID opportunityId);

    List<Quotation> findByParentQuotationId(UUID parentQuotationId);

    @Query("SELECT q FROM Quotation q WHERE q.companyId = :companyId AND q.status IN (:statuses) ORDER BY q.createdAt DESC")
    List<Quotation> findByCompanyIdAndStatusIn(@Param("companyId") UUID companyId, @Param("statuses") List<QuotationStatus> statuses);

    @Query("SELECT q FROM Quotation q WHERE q.companyId = :companyId AND q.status = :status AND q.validUntil < :today ORDER BY q.validUntil")
    List<Quotation> findExpiredByCompanyId(@Param("companyId") UUID companyId, @Param("status") QuotationStatus status, @Param("today") LocalDate today);

    @Query("SELECT q FROM Quotation q WHERE q.companyId = :companyId AND q.validUntil BETWEEN :start AND :end AND q.status = :status ORDER BY q.validUntil")
    List<Quotation> findExpiringInPeriod(@Param("companyId") UUID companyId, @Param("start") LocalDate start, @Param("end") LocalDate end, @Param("status") QuotationStatus status);

    @Query("SELECT q FROM Quotation q WHERE q.companyId = :companyId AND q.createdBy.id = :salesPersonId AND q.status IN (:statuses) ORDER BY q.createdAt DESC")
    List<Quotation> findBySalesPersonAndStatuses(@Param("companyId") UUID companyId, @Param("salesPersonId") UUID salesPersonId, @Param("statuses") List<QuotationStatus> statuses);

    @Query("SELECT q FROM Quotation q WHERE q.companyId = :companyId ORDER BY q.createdAt DESC")
    Page<Quotation> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(q) FROM Quotation q WHERE q.companyId = :companyId AND q.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") QuotationStatus status);

    @Query("SELECT MAX(q.version) FROM Quotation q WHERE q.parentQuotationId = :parentId")
    Integer findMaxVersionByParentId(@Param("parentId") UUID parentId);

    boolean existsByNumberAndCompanyIdAndIdNot(String number, UUID companyId, UUID id);
}