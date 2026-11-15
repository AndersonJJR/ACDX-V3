package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.CommissionRule;
import com.erp.brazil.sales.domain.model.CommissionRule.RuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, UUID> {

    Optional<CommissionRule> findByRuleCodeAndCompanyId(String ruleCode, UUID companyId);

    List<CommissionRule> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<CommissionRule> findBySalesTeamId(UUID salesTeamId);

    List<CommissionRule> findBySalesPersonId(UUID salesPersonId);

    List<CommissionRule> findByTerritoryId(UUID territoryId);

    @Query("SELECT cr FROM CommissionRule cr WHERE cr.companyId = :companyId AND cr.isActive = true " +
           "AND cr.validFrom <= :date AND (cr.validUntil IS NULL OR cr.validUntil >= :date) " +
           "ORDER BY cr.priority DESC, cr.validFrom DESC")
    List<CommissionRule> findApplicableRules(@Param("companyId") UUID companyId, @Param("date") LocalDate date);

    @Query("SELECT cr FROM CommissionRule cr WHERE cr.companyId = :companyId AND cr.isActive = true " +
           "AND cr.validFrom <= :date AND (cr.validUntil IS NULL OR cr.validUntil >= :date) " +
           "AND (cr.salesPerson.id = :salesPersonId OR cr.salesTeam.id = :salesTeamId OR cr.territory.id = :territoryId OR " +
           "     (cr.salesPerson IS NULL AND cr.salesTeam IS NULL AND cr.territory IS NULL)) " +
           "ORDER BY cr.priority DESC, " +
           "CASE WHEN cr.salesPerson IS NOT NULL THEN 1 " +
           "     WHEN cr.salesTeam IS NOT NULL THEN 2 " +
           "     WHEN cr.territory IS NOT NULL THEN 3 " +
           "     ELSE 4 END")
    List<CommissionRule> findApplicableRulesForSalesPerson(
            @Param("companyId") UUID companyId,
            @Param("date") LocalDate date,
            @Param("salesPersonId") UUID salesPersonId,
            @Param("salesTeamId") UUID salesTeamId,
            @Param("territoryId") UUID territoryId);

    @Query("SELECT cr FROM CommissionRule cr WHERE cr.companyId = :companyId AND cr.type = :type ORDER BY cr.name")
    List<CommissionRule> findByCompanyIdAndType(@Param("companyId") UUID companyId, @Param("type") RuleType type);

    Page<CommissionRule> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    boolean existsByRuleCodeAndCompanyIdAndIdNot(String ruleCode, UUID companyId, UUID id);
}