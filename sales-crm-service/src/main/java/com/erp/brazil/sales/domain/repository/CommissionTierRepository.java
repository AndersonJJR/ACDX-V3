package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.CommissionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommissionTierRepository extends JpaRepository<CommissionTier, UUID> {

    List<CommissionTier> findByCommissionRuleIdOrderByMinAmountAsc(UUID commissionRuleId);

    void deleteByCommissionRuleId(UUID commissionRuleId);
}