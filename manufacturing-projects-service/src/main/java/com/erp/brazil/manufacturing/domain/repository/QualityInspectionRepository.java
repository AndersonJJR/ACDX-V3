package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.QualityInspection;
import com.erp.brazil.manufacturing.domain.model.QualityInspection.InspectionType;
import com.erp.brazil.manufacturing.domain.model.QualityInspection.InspectionStatus;
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
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, UUID> {

    Optional<QualityInspection> findByInspectionNumber(String inspectionNumber);

    List<QualityInspection> findByInspectionType(InspectionType inspectionType);

    List<QualityInspection> findByStatus(InspectionStatus status);

    List<QualityInspection> findByProductId(UUID productId);

    List<QualityInspection> findByProductionOrderId(UUID productionOrderId);

    List<QualityInspection> findByWorkOrderId(UUID workOrderId);

    List<QualityInspection> findByGoodsReceiptId(UUID goodsReceiptId);

    List<QualityInspection> findBySubcontractingReceiptId(UUID subcontractingReceiptId);

    Page<QualityInspection> findByStatus(InspectionStatus status, Pageable pageable);

    Page<QualityInspection> findByInspectionType(InspectionType inspectionType, Pageable pageable);

    @Query("SELECT qi FROM QualityInspection qi WHERE qi.inspectionDate BETWEEN :startDate AND :endDate ORDER BY qi.inspectionDate DESC")
    List<QualityInspection> findByInspectionDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT qi FROM QualityInspection qi WHERE qi.inspectorId = :inspectorId AND qi.status IN ('PENDING', 'IN_PROGRESS') ORDER BY qi.inspectionDate ASC")
    List<QualityInspection> findAssignedToInspector(@Param("inspectorId") String inspectorId);

    @Query("SELECT qi FROM QualityInspection qi WHERE qi.lotNumber = :lotNumber ORDER BY qi.inspectionDate DESC")
    List<QualityInspection> findByLotNumber(@Param("lotNumber") String lotNumber);

    boolean existsByInspectionNumber(String inspectionNumber);
}