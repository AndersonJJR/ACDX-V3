package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.UserCompanyRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for UserCompanyRole entity.
 */
@Repository
public interface UserCompanyRoleJpaRepository extends JpaRepository<UserCompanyRole, String> {

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.userId = :userId AND ucr.deleted = false")
    List<UserCompanyRole> findByUserId(@Param("userId") String userId);

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.userId = :userId AND ucr.companyId = :companyId AND ucr.deleted = false")
    List<UserCompanyRole> findByUserIdAndCompanyId(@Param("userId") String userId, @Param("companyId") String companyId);

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.userId = :userId AND ucr.companyId = :companyId AND ucr.deleted = false AND ucr.validFrom <= :now AND (ucr.validUntil IS NULL OR ucr.validUntil >= :now)")
    List<UserCompanyRole> findValidByUserIdAndCompanyId(@Param("userId") String userId, @Param("companyId") String companyId, @Param("now") OffsetDateTime now);

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.companyId = :companyId AND ucr.deleted = false")
    List<UserCompanyRole> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.roleId = :roleId AND ucr.deleted = false")
    List<UserCompanyRole> findByRoleId(@Param("roleId") String roleId);

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.branchId = :branchId AND ucr.deleted = false")
    List<UserCompanyRole> findByBranchId(@Param("branchId") String branchId);

    @Query("SELECT ucr FROM UserCompanyRole ucr WHERE ucr.departmentId = :departmentId AND ucr.deleted = false")
    List<UserCompanyRole> findByDepartmentId(@Param("departmentId") String departmentId);

    boolean existsByUserIdAndCompanyIdAndRoleIdAndBranchIdAndDeletedFalse(
            @Param("userId") String userId,
            @Param("companyId") String companyId,
            @Param("roleId") String roleId,
            @Param("branchId") String branchId);
}