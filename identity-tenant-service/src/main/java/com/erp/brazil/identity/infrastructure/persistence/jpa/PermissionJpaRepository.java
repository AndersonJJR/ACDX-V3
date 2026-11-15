package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Permission entity.
 */
@Repository
public interface PermissionJpaRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByCodeAndTenantIdAndDeletedFalse(@Param("code") String code, @Param("tenantId") String tenantId);

    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId AND p.deleted = false")
    List<Permission> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId AND p.deleted = false")
    Page<Permission> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.deleted = false")
    List<Permission> findByResource(@Param("resource") String resource);

    @Query("SELECT p FROM Permission p WHERE p.action = :action AND p.deleted = false")
    List<Permission> findByAction(@Param("action") String action);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deleted = false")
    Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    @Query("SELECT p FROM Permission p WHERE p.isSystem = true AND p.deleted = false")
    List<Permission> findSystemPermissions();

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);
}