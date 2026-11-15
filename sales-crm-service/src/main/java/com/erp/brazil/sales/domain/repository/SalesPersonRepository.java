package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesPerson;
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
public interface SalesPersonRepository extends JpaRepository<SalesPerson, UUID> {

    Optional<SalesPerson> findByEmployeeIdAndCompanyId(String employeeId, UUID companyId);

    Optional<SalesPerson> findByUserId(String userId);

    List<SalesPerson> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<SalesPerson> findByTerritoryId(UUID territoryId);

    List<SalesPerson> findBySalesTeamId(UUID salesTeamId);

    @Query("SELECT sp FROM SalesPerson sp WHERE sp.companyId = :companyId AND sp.isActive = true ORDER BY sp.fullName")
    List<SalesPerson> findActiveByCompanyIdOrderByName(UUID companyId);

    Page<SalesPerson> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByEmployeeIdAndCompanyIdAndIdNot(String employeeId, UUID companyId, UUID id);

    boolean existsByUserIdAndIdNot(String userId, UUID id);
}