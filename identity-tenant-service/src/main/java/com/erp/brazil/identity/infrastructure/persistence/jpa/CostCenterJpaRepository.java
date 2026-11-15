package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.CostCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for CostCenter entity.
 */
@Repository
public interface CostCenterJpaRepository extends JpaRepository<CostCenter, String> {

    Optional<CostCenter> findByCodeAndCompanyIdAndDeletedFalse(@Param("code") String code, @Param("companyId") String companyId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.tenantId = :tenantId AND cc.deleted = false")
    List<CostCenter> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.companyId = :companyId AND cc.deleted = false")
    List<CostCenter> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.parentId = :parentId AND cc.deleted = false")
    List<CostCenter> findByParentId(@Param("parentId") String parentId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.managerId = :managerId AND cc.deleted = false")
    List<CostCenter> findByManagerId(@Param("managerId") String managerId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.budgetOwnerId = :budgetOwnerId AND cc.deleted = false")
    List<CostCenter> findByBudgetOwnerId(@Param("budgetOwnerId") String budgetOwnerId);

    @Query("SELECT cc FROM CostCenter cc WHERE cc.isActive = true AND cc.deleted = false")
    List<CostCenter> findActive();

    boolean existsByCodeAndCompanyIdAndDeletedFalse(String code, String companyId);
}