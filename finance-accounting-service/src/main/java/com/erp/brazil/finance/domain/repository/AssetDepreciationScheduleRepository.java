package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Asset;
import com.erp.brazil.finance.domain.model.AssetDepreciationSchedule;
import com.erp.brazil.finance.domain.model.DepreciationScheduleStatus;
import com.erp.brazil.finance.domain.model.FiscalPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AssetDepreciationScheduleRepository extends JpaRepository<AssetDepreciationSchedule, UUID> {

    List<AssetDepreciationSchedule> findByAssetOrderByPeriodNumber(Asset asset);

    List<AssetDepreciationSchedule> findByAssetAndStatus(Asset asset, DepreciationScheduleStatus status);

    List<AssetDepreciationSchedule> findByFiscalPeriodAndStatus(FiscalPeriod fiscalPeriod, DepreciationScheduleStatus status);

    List<AssetDepreciationSchedule> findByAssetAndFiscalPeriod(Asset asset, FiscalPeriod fiscalPeriod);

    @Query("SELECT s FROM AssetDepreciationSchedule s WHERE s.asset = :asset AND s.fiscalPeriod = :fiscalPeriod AND s.status = :status")
    Optional<AssetDepreciationSchedule> findByAssetAndFiscalPeriodAndStatus(@Param("asset") Asset asset, @Param("fiscalPeriod") FiscalPeriod fiscalPeriod, @Param("status") DepreciationScheduleStatus status);

    List<AssetDepreciationSchedule> findByStatusAndScheduledDateLessThanEqual(DepreciationScheduleStatus status, java.time.LocalDate date);

    Page<AssetDepreciationSchedule> findByAsset(Asset asset, Pageable pageable);

    Page<AssetDepreciationSchedule> findByFiscalPeriod(FiscalPeriod fiscalPeriod, Pageable pageable);
}