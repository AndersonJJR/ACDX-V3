package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Department entity.
 */
@Repository
public interface DepartmentJpaRepository extends JpaRepository<Department, String> {

    Optional<Department> findByCodeAndCompanyIdAndBranchIdAndDeletedFalse(
            @Param("code") String code,
            @Param("companyId") String companyId,
            @Param("branchId") String branchId);

    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND d.deleted = false")
    List<Department> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT d FROM Department d WHERE d.companyId = :companyId AND d.deleted = false")
    List<Department> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT d FROM Department d WHERE d.branchId = :branchId AND d.deleted = false")
    List<Department> findByBranchId(@Param("branchId") String branchId);

    @Query("SELECT d FROM Department d WHERE d.parentId = :parentId AND d.deleted = false")
    List<Department> findByParentId(@Param("parentId") String parentId);

    @Query("SELECT d FROM Department d WHERE d.managerId = :managerId AND d.deleted = false")
    List<Department> findByManagerId(@Param("managerId") String managerId);

    @Query("SELECT d FROM Department d WHERE d.isActive = true AND d.deleted = false")
    List<Department> findActive();

    boolean existsByCodeAndCompanyIdAndBranchIdAndDeletedFalse(String code, String companyId, String branchId);
}