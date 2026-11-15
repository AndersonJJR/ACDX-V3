package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.LeadActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadActivityRepository extends JpaRepository<LeadActivity, UUID> {

    List<LeadActivity> findByLeadIdOrderByCreatedAtDesc(UUID leadId);

    Page<LeadActivity> findByLeadId(UUID leadId, Pageable pageable);

    List<LeadActivity> findByCreatedByIdOrderByCreatedAtDesc(UUID salesPersonId);

    @Query("SELECT a FROM LeadActivity a WHERE a.lead.id = :leadId AND a.type = :type ORDER BY a.createdAt DESC")
    List<LeadActivity> findByLeadIdAndType(@Param("leadId") UUID leadId, @Param("type") String type);
}