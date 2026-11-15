package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Asset;
import com.erp.brazil.finance.domain.model.AssetDepreciationSchedule;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetDepreciationScheduleRepository extends JpaRepository<AssetDepreciationSchedule, UUID> {

    List<AssetDepreciationSchedule> findByAssetOrderByPeriodStart(Asset asset);

    List<AssetDepreciationSchedule> findByAssetAndPostedFalseOrderByPeriodStart(Asset asset);

    @Query("SELECT s FROM AssetDepreciationSchedule s WHERE s.asset = :asset AND s.periodStart = :periodStart AND s.periodEnd = :periodEnd")
    Optional<AssetDepreciationSchedule> findByAssetAndPeriod(@Param("asset") Asset asset, @Param("periodStart") LocalDate periodStart, @Param("periodEnd") LocalDate periodEnd);

    @Query("SELECT s FROM AssetDepreciationSchedule s WHERE s.posted = false AND s.periodEnd <= :date ORDER BY s.periodStart")
    List<AssetDepreciationSchedule> findDueSchedules(@Param("date") LocalDate date);

    Page<AssetDepreciationSchedule> findByAsset(Asset asset, Pageable pageable);

    Page<AssetDepreciationSchedule> findByFiscalPeriod(FiscalPeriod fiscalPeriod, Pageable pageable);
}