package com.erp.brazil.identity.infrastructure.persistence.jpa;

import com.erp.brazil.identity.domain.model.BootInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for BootInfo entity.
 */
@Repository
public interface BootInfoJpaRepository extends JpaRepository<BootInfo, String> {

    Optional<BootInfo> findById(String id);
}