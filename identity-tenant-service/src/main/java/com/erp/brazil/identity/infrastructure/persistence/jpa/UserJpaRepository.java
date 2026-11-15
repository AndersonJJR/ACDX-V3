package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for User entity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByCpfAndDeletedFalse(String cpf);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false")
    List<User> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false")
    Page<User> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.companyId = :companyId AND u.deleted = false")
    List<User> findByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT u FROM User u WHERE u.companyId = :companyId AND u.deleted = false")
    Page<User> findByCompanyId(@Param("companyId") String companyId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deleted = false")
    List<User> findByStatus(@Param("status") User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.deleted = false")
    List<User> findUnverifiedUsers();

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByCpfAndDeletedFalse(String cpf);
}