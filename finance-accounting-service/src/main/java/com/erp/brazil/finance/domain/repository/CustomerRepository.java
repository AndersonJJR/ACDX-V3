package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Customer;
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
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<Customer> findByDocumentTypeAndDocumentNumberAndCompanyId(DocumentType documentType, String documentNumber, UUID companyId);

    List<Customer> findByCompanyIdOrderByName(UUID companyId);

    List<Customer> findByCompanyIdAndActiveTrueOrderByName(UUID companyId);

    @Query("SELECT c FROM Customer c WHERE c.companyId = :companyId AND (c.name LIKE %:search% OR c.tradeName LIKE %:search% OR c.documentNumber LIKE %:search%) ORDER BY c.name")
    Page<Customer> searchByCompanyIdAndTerm(@Param("companyId") UUID companyId, @Param("search") String search, Pageable pageable);

    Page<Customer> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);

    boolean existsByDocumentTypeAndDocumentNumberAndCompanyId(DocumentType documentType, String documentNumber, UUID companyId);
}