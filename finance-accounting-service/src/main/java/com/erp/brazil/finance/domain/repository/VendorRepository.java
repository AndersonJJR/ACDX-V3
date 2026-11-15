package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Vendor;
import com.erp.brazil.finance.domain.model.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    Optional<Vendor> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<Vendor> findByDocumentTypeAndDocumentNumberAndCompanyId(DocumentType documentType, String documentNumber, UUID companyId);

    List<Vendor> findByCompanyIdOrderByName(UUID companyId);

    List<Vendor> findByCompanyIdAndActiveTrueOrderByName(UUID companyId);

    @Query("SELECT v FROM Vendor v WHERE v.companyId = :companyId AND (v.name LIKE %:search% OR v.tradeName LIKE %:search% OR v.documentNumber LIKE %:search%) ORDER BY v.name")
    Page<Vendor> searchByCompanyIdAndTerm(@Param("companyId") UUID companyId, @Param("search") String search, Pageable pageable);

    Page<Vendor> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByDocumentTypeAndDocumentNumberAndCompanyId(DocumentType documentType, String documentNumber, UUID companyId);
}