package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.common.domain.model.NumberingSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for NumberingSeries entity.
 * Provides locking for thread-safe number generation.
 */
@Repository
public interface NumberingSeriesJpaRepository extends JpaRepository<NumberingSeries, String> {

    Optional<NumberingSeries> findByCodeAndTenantIdAndCompanyIdAndBranchIdAndDeletedFalse(
            @Param("code") String code,
            @Param("tenantId") String tenantId,
            @Param("companyId") String companyId,
            @Param("branchId") String branchId);

    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.tenantId = :tenantId AND ns.deleted = false")
    List<NumberingSeries> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.companyId = :companyId AND ns.deleted = false")
    List<NumberingSeries> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.branchId = :branchId AND ns.deleted = false")
    List<NumberingSeries> findByBranchId(@Param("branchId") String branchId);

    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.isActive = true AND ns.deleted = false")
    List<NumberingSeries> findActive();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ns FROM NumberingSeries ns WHERE ns.id = :id AND ns.deleted = false")
    Optional<NumberingSeries> findByIdForUpdate(@Param("id") String id);

    boolean existsByCodeAndTenantIdAndCompanyIdAndBranchIdAndDeletedFalse(
            String code, String tenantId, String companyId, String branchId);
}