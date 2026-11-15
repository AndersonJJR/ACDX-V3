package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesPipeline;
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
public interface SalesPipelineRepository extends JpaRepository<SalesPipeline, UUID> {

    Optional<SalesPipeline> findByCodeAndCompanyId(String code, UUID companyId);

    Optional<SalesPipeline> findByCompanyIdAndIsDefaultTrue(UUID companyId);

    List<SalesPipeline> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<SalesPipeline> findBySalesTeamId(UUID salesTeamId);

    @Query("SELECT p FROM SalesPipeline p WHERE p.companyId = :companyId AND p.isActive = true ORDER BY p.name")
    List<SalesPipeline> findActiveByCompanyIdOrderByName(UUID companyId);

    Page<SalesPipeline> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);
}