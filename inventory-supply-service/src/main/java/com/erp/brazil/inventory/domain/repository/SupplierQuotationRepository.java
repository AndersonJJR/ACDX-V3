package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.SupplierQuotation;
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
public interface SupplierQuotationRepository extends JpaRepository<SupplierQuotation, UUID> {

    Optional<SupplierQuotation> findByCompanyIdAndQuotationNo(UUID companyId, String quotationNo);

    List<SupplierQuotation> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    Page<SupplierQuotation> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId, Pageable pageable);

    List<SupplierQuotation> findByCompanyIdAndStatus(UUID companyId, SupplierQuotation.QuotationStatus status);

    List<SupplierQuotation> findByCompanyIdAndValidTillBefore(UUID companyId, LocalDate validTill);

    List<SupplierQuotation> findByCompanyIdAndQuotationDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT sq FROM SupplierQuotation sq WHERE sq.companyId = :companyId AND sq.supplierId = :supplierId AND sq.status IN ('DRAFT', 'SUBMITTED', 'ACCEPTED') AND sq.validTill >= :today ORDER BY sq.grandTotal ASC")
    List<SupplierQuotation> findActiveQuotationsForSupplier(@Param("companyId") UUID companyId, @Param("supplierId") UUID supplierId, @Param("today") LocalDate today);

    boolean existsByCompanyIdAndQuotationNoAndIdNot(UUID companyId, String quotationNo, UUID id);
}