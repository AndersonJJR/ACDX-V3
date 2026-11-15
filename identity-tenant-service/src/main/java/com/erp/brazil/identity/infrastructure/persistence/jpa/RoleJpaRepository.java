package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Role entity.
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<Role, String> {

    Optional<Role> findByCodeAndTenantIdAndDeletedFalse(@Param("code") String code, @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId AND r.deleted = false")
    List<Role> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId AND r.deleted = false")
    Page<Role> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT r FROM Role r WHERE r.companyId = :companyId AND r.deleted = false")
    List<Role> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT r FROM Role r WHERE r.roleType = :roleType AND r.deleted = false")
    List<Role> findByRoleType(@Param("roleType") Role.RoleType roleType);

    @Query("SELECT r FROM Role r WHERE r.isDefault = true AND r.deleted = false")
    List<Role> findDefaultRoles();

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);
}