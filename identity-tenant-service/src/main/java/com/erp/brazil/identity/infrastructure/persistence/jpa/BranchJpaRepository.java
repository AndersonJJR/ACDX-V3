package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Branch entity.
 */
@Repository
public interface BranchJpaRepository extends JpaRepository<Branch, String> {

    Optional<Branch> findByCodeAndCompanyIdAndDeletedFalse(@Param("code") String code, @Param("companyId") String companyId);

    Optional<Branch> findByCnpjAndDeletedFalse(String cnpj);

    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND b.deleted = false")
    List<Branch> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT b FROM Branch b WHERE b.tenantId = :tenantId AND b.deleted = false")
    Page<Branch> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.companyId = :companyId AND b.deleted = false")
    List<Branch> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT b FROM Branch b WHERE b.companyId = :companyId AND b.deleted = false")
    Page<Branch> findByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.companyId = :companyId AND b.isHeadquarters = true AND b.deleted = false")
    Optional<Branch> findHeadquartersByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT b FROM Branch b WHERE b.status = :status AND b.deleted = false")
    List<Branch> findByStatus(@Param("status") Branch.BranchStatus status);

    boolean existsByCodeAndCompanyIdAndDeletedFalse(String code, String companyId);

    boolean existsByCnpjAndDeletedFalse(String cnpj);
}