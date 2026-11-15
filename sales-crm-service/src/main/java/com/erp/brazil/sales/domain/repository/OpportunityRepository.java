package com.erp.brazil.sales.domain.repository;

import com.erp.brazil.sales.domain.model.Opportunity;
import com.erp.brazil.sales.domain.model.OpportunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    Optional<Opportunity> findByOpportunityCodeAndCompanyId(String opportunityCode, UUID companyId);

    List<Opportunity> findByCompanyIdAndStatus(UUID companyId, OpportunityStatus status);

    List<Opportunity> findByAssignedSalesPersonId(UUID salesPersonId);

    List<Opportunity> findByCustomerId(UUID customerId);

    List<Opportunity> findByPipelineIdAndCurrentStageId(UUID pipelineId, UUID stageId);

    List<Opportunity> findByLeadId(UUID leadId);

    @Query("SELECT o FROM Opportunity o WHERE o.companyId = :companyId AND o.status IN (:statuses) ORDER BY o.weightedAmount DESC, o.expectedCloseDate ASC")
    List<Opportunity> findByCompanyIdAndStatusIn(@Param("companyId") UUID companyId, @Param("statuses") List<OpportunityStatus> statuses);

    @Query("SELECT o FROM Opportunity o WHERE o.companyId = :companyId AND o.assignedSalesPersonId = :salesPersonId AND o.status IN (:statuses) ORDER BY o.weightedAmount DESC")
    List<Opportunity> findActiveBySalesPerson(@Param("companyId") UUID companyId, @Param("salesPersonId") UUID salesPersonId, @Param("statuses") List<OpportunityStatus> statuses);

    @Query("SELECT o FROM Opportunity o WHERE o.companyId = :companyId AND o.status = :status AND o.expectedCloseDate < :now ORDER BY o.expectedCloseDate")
    List<Opportunity> findOverdueByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") OpportunityStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM Opportunity o WHERE o.companyId = :companyId AND o.expectedCloseDate BETWEEN :start AND :end AND o.status IN (:statuses) ORDER BY o.expectedCloseDate")
    List<Opportunity> findClosingInPeriod(@Param("companyId") UUID companyId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statuses") List<OpportunityStatus> statuses);

    @Query("SELECT SUM(o.weightedAmount) FROM Opportunity o WHERE o.companyId = :companyId AND o.status IN (:statuses)")
    BigDecimal sumWeightedAmountByCompanyAndStatuses(@Param("companyId") UUID companyId, @Param("statuses") List<OpportunityStatus> statuses);

    @Query("SELECT o FROM Opportunity o WHERE o.companyId = :companyId ORDER BY o.createdAt DESC")
    Page<Opportunity> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Opportunity o WHERE o.companyId = :companyId AND o.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") OpportunityStatus status);

    @Query("SELECT o FROM Opportunity o WHERE o.companyId = :companyId AND o.opportunityCode LIKE :pattern ORDER BY o.createdAt DESC")
    List<Opportunity> searchByOpportunityCode(@Param("companyId") UUID companyId, @Param("pattern") String pattern);

    @Query("SELECT NEW com.erp.brazil.sales.presentation.dto.KanbanOpportunityDTO(o.id, o.opportunityCode, o.title, o.amount, o.probability, o.weightedAmount, o.expectedCloseDate, o.customer.tradeName, o.assignedSalesPerson.fullName, o.currentStage.id, o.currentStage.name, o.currentStage.color) FROM Opportunity o WHERE o.pipeline.id = :pipelineId AND o.status IN (:statuses) ORDER BY o.currentStage.orderIndex, o.weightedAmount DESC")
    List<com.erp.brazil.sales.presentation.dto.KanbanOpportunityDTO> findKanbanDataByPipeline(@Param("pipelineId") UUID pipelineId, @Param("statuses") List<OpportunityStatus> statuses);

    boolean existsByOpportunityCodeAndCompanyIdAndIdNot(String opportunityCode, UUID companyId, UUID id);
}