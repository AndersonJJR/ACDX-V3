package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.OpportunityProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityProductRepository extends JpaRepository<OpportunityProduct, UUID> {

    List<OpportunityProduct> findByOpportunityId(UUID opportunityId);

    void deleteByOpportunityId(UUID opportunityId);
}