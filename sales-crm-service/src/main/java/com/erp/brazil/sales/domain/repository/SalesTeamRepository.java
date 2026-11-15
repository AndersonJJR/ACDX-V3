package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.SalesTeam;
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
public interface SalesTeamRepository extends JpaRepository<SalesTeam, UUID> {

    Optional<SalesTeam> findByCodeAndCompanyId(String code, UUID companyId);

    List<SalesTeam> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<SalesTeam> findByManagerId(UUID managerId);

    @Query("SELECT st FROM SalesTeam st WHERE st.companyId = :companyId AND st.isActive = true ORDER BY st.name")
    List<SalesTeam> findActiveByCompanyIdOrderByName(UUID companyId);

    @Query("SELECT st FROM SalesTeam st WHERE :salesPersonId MEMBER OF st.members")
    List<SalesTeam> findByMemberId(@Param("salesPersonId") UUID salesPersonId);

    Page<SalesTeam> findByCompanyId(UUID companyId, Pageable pageable);

    boolean existsByCodeAndCompanyIdAndIdNot(String code, UUID companyId, UUID id);
}