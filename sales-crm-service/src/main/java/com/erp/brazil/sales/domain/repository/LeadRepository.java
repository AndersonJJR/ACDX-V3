package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Lead;
import com.erp.brazil.sales.domain.model.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Optional<Lead> findByLeadCodeAndCompanyId(String leadCode, UUID companyId);

    List<Lead> findByCompanyIdAndStatus(UUID companyId, LeadStatus status);

    List<Lead> findByAssignedSalesPersonId(UUID salesPersonId);

    List<Lead> findByTerritoryId(UUID territoryId);

    List<Lead> findByPipelineIdAndCurrentStageId(UUID pipelineId, UUID stageId);

    @Query("SELECT l FROM Lead l WHERE l.companyId = :companyId AND l.status IN (:statuses) ORDER BY l.createdAt DESC")
    List<Lead> findByCompanyIdAndStatusIn(@Param("companyId") UUID companyId, @Param("statuses") List<LeadStatus> statuses);

    @Query("SELECT l FROM Lead l WHERE l.companyId = :companyId AND l.status = :status AND l.expectedCloseDate < :now ORDER BY l.expectedCloseDate")
    List<Lead> findOverdueByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") LeadStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT l FROM Lead l WHERE l.companyId = :companyId AND l.assignedSalesPersonId = :salesPersonId AND l.status IN (:statuses) ORDER BY l.score DESC, l.expectedCloseDate ASC")
    List<Lead> findActiveBySalesPerson(@Param("companyId") UUID companyId, @Param("salesPersonId") UUID salesPersonId, @Param("statuses") List<LeadStatus> statuses);

    @Query("SELECT l FROM Lead l WHERE l.companyId = :companyId ORDER BY l.createdAt DESC")
    Page<Lead> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.companyId = :companyId AND l.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") LeadStatus status);

    @Query("SELECT l FROM Lead l WHERE l.companyId = :companyId AND l.leadCode LIKE :pattern ORDER BY l.createdAt DESC")
    List<Lead> searchByLeadCode(@Param("companyId") UUID companyId, @Param("pattern") String pattern);

    boolean existsByLeadCodeAndCompanyIdAndIdNot(String leadCode, UUID companyId, UUID id);
}