package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.CorrectiveAction;
import com.erp.brazil.manufacturing.domain.model.CorrectiveAction.ActionStatus;
import com.erp.brazil.manufacturing.domain.model.CorrectiveAction.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CorrectiveActionRepository extends JpaRepository<CorrectiveAction, UUID> {

    List<CorrectiveAction> findByNonConformanceId(UUID nonConformanceId);

    List<CorrectiveAction> findByAssignedTo(String assignedTo);

    List<CorrectiveAction> findByStatus(ActionStatus status);

    List<CorrectiveAction> findByType(ActionType type);

    Page<CorrectiveAction> findByStatus(ActionStatus status, Pageable pageable);

    Page<CorrectiveAction> findByAssignedTo(String assignedTo, Pageable pageable);

    @Query("SELECT ca FROM CorrectiveAction ca WHERE ca.plannedCompletionDate < :now AND ca.status IN ('PLANNED', 'IN_PROGRESS')")
    List<CorrectiveAction> findOverdue(@Param("now") LocalDateTime now);

    @Query("SELECT ca FROM CorrectiveAction ca WHERE ca.assignedTo = :assignedTo AND ca.status IN ('PLANNED', 'IN_PROGRESS') ORDER BY ca.plannedCompletionDate ASC")
    List<CorrectiveAction> findOpenByAssignee(@Param("assignedTo") String assignedTo);

    @Query("SELECT ca FROM CorrectiveAction ca WHERE ca.effectivenessCheckDate < :now AND ca.status = 'VERIFIED'")
    List<CorrectiveAction> findDueForEffectivenessCheck(@Param("now") LocalDateTime now);
}