package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Territory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, UUID> {

    Optional<Territory> findByCodeAndCompanyId(String code, UUID companyId);

    List<Territory> findByCompanyIdAndActiveTrue(UUID companyId);

    List<Territory> findByParentTerritoryId(UUID parentTerritoryId);

    @Query("SELECT t FROM Territory t WHERE t.companyId = :companyId AND t.active = true ORDER BY t.code")
    List<Territory> findActiveByCompanyIdOrderByCode(UUID companyId);

    Page<Territory> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);
}