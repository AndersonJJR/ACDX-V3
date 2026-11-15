package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.QualityInspectionResult;
import com.erp.brazil.manufacturing.domain.model.QualityInspectionResult.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualityInspectionResultRepository extends JpaRepository<QualityInspectionResult, UUID> {

    List<QualityInspectionResult> findByQualityInspectionId(UUID qualityInspectionId);

    List<QualityInspectionResult> findByCharacteristicId(UUID characteristicId);

    List<QualityInspectionResult> findByQualityInspectionIdAndResult(UUID qualityInspectionId, Result result);

    @Query("SELECT COUNT(qir) FROM QualityInspectionResult qir WHERE qir.qualityInspection.id = :inspectionId AND qir.result = 'FAIL'")
    long countFailuresByInspection(@Param("inspectionId") UUID inspectionId);

    @Query("SELECT COUNT(qir) FROM QualityInspectionResult qir WHERE qir.qualityInspection.id = :inspectionId")
    long countTotalByInspection(@Param("inspectionId") UUID inspectionId);
}