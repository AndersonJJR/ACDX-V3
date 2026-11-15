package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Company entity.
 */
@Repository
public interface CompanyJpaRepository extends JpaRepository<Company, String> {

    Optional<Company> findByCodeAndDeletedFalse(String code);

    Optional<Company> findByCnpjAndDeletedFalse(String cnpj);

    Optional<Company> findByEmailAndDeletedFalse(String email);

    @Query("SELECT c FROM Company c WHERE c.tenantId = :tenantId AND c.deleted = false")
    List<Company> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Company c WHERE c.tenantId = :tenantId AND c.deleted = false")
    Page<Company> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.companyId = :companyId AND c.deleted = false")
    List<Company> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT c FROM Company c WHERE c.status = :status AND c.deleted = false")
    List<Company> findByStatus(@Param("status") Company.CompanyStatus status);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCnpjAndDeletedFalse(String cnpj);

    boolean existsByEmailAndDeletedFalse(String email);
}