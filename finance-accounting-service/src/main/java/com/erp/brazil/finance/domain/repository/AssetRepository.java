package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.Asset;
import com.erp.brazil.finance.domain.model.AssetCategory;
import com.erp.brazil.finance.domain.model.AssetLocation;
import com.erp.brazil.finance.domain.model.AssetStatus;
import com.erp.brazil.finance.domain.model.ChartOfAccounts;
import com.erp.brazil.finance.domain.model.CostCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByCode(String code);

    Optional<Asset> findByAssetTag(String assetTag);

    List<Asset> findByCategory(AssetCategory category);

    List<Asset> findByLocation(AssetLocation location);

    List<Asset> findByCostCenter(CostCenter costCenter);

    List<Asset> findByAccount(ChartOfAccounts account);

    List<Asset> findByStatus(AssetStatus status);

    List<Asset> findByStatusIn(List<AssetStatus> statuses);

    List<Asset> findByAcquisitionDateBetween(LocalDate startDate, LocalDate endDate);

    List<Asset> findByDepreciationStartDateLessThanEqualAndStatus(LocalDate date, AssetStatus status);

    @Query("SELECT a FROM Asset a WHERE a.status = :status AND a.depreciationStartDate <= :date AND a.disposalDate IS NULL")
    List<Asset> findAssetsToDepreciate(@Param("status") AssetStatus status, @Param("date") LocalDate date);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    Page<Asset> findByCategory(AssetCategory category, Pageable pageable);

    Page<Asset> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);

    boolean existsByAssetTagAndIdNot(String assetTag, UUID id);
}