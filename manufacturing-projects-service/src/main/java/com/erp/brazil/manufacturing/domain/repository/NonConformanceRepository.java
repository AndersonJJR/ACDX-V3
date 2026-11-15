package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.NonConformance;
import com.erp.brazil.manufacturing.domain.model.NonConformance.NCStatus;
import com.erp.brazil.manufacturing.domain.model.NonConformance.Severity;
import com.erp.brazil.manufacturing.domain.model.NonConformance.Source;
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
public interface NonConformanceRepository extends JpaRepository<NonConformance, UUID> {

    Optional<NonConformance> findByNcNumber(String ncNumber);

    List<NonConformance> findByQualityInspectionId(UUID qualityInspectionId);

    List<NonConformance> findByProductId(UUID productId);

    List<NonConformance> findByStatus(NCStatus status);

    List<NonConformance> findBySeverity(Severity severity);

    List<NonConformance> findByAssignedTo(String assignedTo);

    List<NonConformance> findBySource(Source source);

    Page<NonConformance> findByStatus(NCStatus status, Pageable pageable);

    @Query("SELECT nc FROM NonConformance nc WHERE nc.identifiedDate BETWEEN :startDate AND :endDate ORDER BY nc.identifiedDate DESC")
    List<NonConformance> findByIdentifiedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT nc FROM NonConformance nc WHERE nc.assignedTo = :assignedTo AND nc.status IN ('OPEN', 'UNDER_INVESTIGATION', 'ROOT_CAUSE_IDENTIFIED', 'CORRECTIVE_ACTION_PLANNED', 'CORRECTIVE_ACTION_IN_PROGRESS') ORDER BY nc.targetResolutionDate ASC")
    List<NonConformance> findOpenByAssignee(@Param("assignedTo") String assignedTo);

    @Query("SELECT nc FROM NonConformance nc WHERE nc.targetResolutionDate < :now AND nc.status NOT IN ('VERIFIED', 'CLOSED', 'CANCELLED')")
    List<NonConformance> findOverdue(@Param("now") LocalDateTime now);

    @Query("SELECT nc FROM NonConformance nc WHERE nc.productionOrderId = :prodOrderId ORDER BY nc.identifiedDate DESC")
    List<NonConformance> findByProductionOrderId(@Param("prodOrderId") UUID prodOrderId);

    boolean existsByNcNumber(String ncNumber);
}