package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.OpportunityActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityActivityRepository extends JpaRepository<OpportunityActivity, UUID> {

    List<OpportunityActivity> findByOpportunityIdOrderByCreatedAtDesc(UUID opportunityId);

    Page<OpportunityActivity> findByOpportunityId(UUID opportunityId, Pageable pageable);

    List<OpportunityActivity> findByCreatedByIdOrderByCreatedAtDesc(UUID salesPersonId);

    @Query("SELECT a FROM OpportunityActivity a WHERE a.opportunity.id = :opportunityId AND a.type = :type ORDER BY a.createdAt DESC")
    List<OpportunityActivity> findByOpportunityIdAndType(@Param("opportunityId") UUID opportunityId, @Param("type") String type);
}