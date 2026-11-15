package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.SupplierQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SupplierQuotationRepository extends JpaRepository<SupplierQuotation, UUID> {

    List<SupplierQuotation> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    List<SupplierQuotation> findByCompanyIdAndStatus(UUID companyId, SupplierQuotation.QuotationStatus status);

    List<SupplierQuotation> findByCompanyIdAndValidTillBefore(UUID companyId, LocalDate date);

    @Query("SELECT q FROM SupplierQuotation q WHERE q.companyId = :companyId AND q.supplierId = :supplierId AND q.status = :status AND q.validTill >= :today")
    List<SupplierQuotation> findValidQuotations(@Param("companyId") UUID companyId, @Param("supplierId") UUID supplierId, @Param("status") SupplierQuotation.QuotationStatus status, @Param("today") LocalDate today);
}