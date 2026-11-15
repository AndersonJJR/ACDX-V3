package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.StockLedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockLedgerEntryRepository extends JpaRepository<StockLedgerEntry, UUID> {

    Page<StockLedgerEntry> findByCompanyIdAndItemIdAndWarehouseId(
            UUID companyId, UUID itemId, UUID warehouseId, Pageable pageable);

    Page<StockLedgerEntry> findByCompanyIdAndVoucher(UUID companyId, String voucherType, String voucherNo, Pageable pageable);

    List<StockLedgerEntry> findByCompanyIdAndItemIdAndWarehouseIdOrderByPostingDateAscPostingTimeAsc(
            UUID companyId, UUID itemId, UUID warehouseId);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.isCancelled = false ORDER BY sle.postingDate DESC, sle.postingTime DESC")
    List<StockLedgerEntry> findLatestEntriesByItemWarehouse(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT COALESCE(SUM(sle.qtyChange), 0) FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.isCancelled = false")
    java.math.BigDecimal getCurrentStockQty(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT COALESCE(SUM(sle.stockValue), 0) FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.isCancelled = false")
    java.math.BigDecimal getCurrentStockValue(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.qtyChange > 0 AND sle.isCancelled = false ORDER BY sle.postingDate ASC, sle.postingTime ASC")
    List<StockLedgerEntry> findIncomingEntriesForFifo(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.postingDate BETWEEN :startDate AND :endDate AND sle.isCancelled = false ORDER BY sle.postingDate ASC, sle.postingTime ASC")
    List<StockLedgerEntry> findByDateRange(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.postingDate BETWEEN :startDate AND :endDate AND sle.isCancelled = false ORDER BY sle.postingDate ASC, sle.postingTime ASC")
    List<StockLedgerEntry> findByCompanyAndDateRange(@Param("companyId") UUID companyId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.batchId = :batchId AND sle.isCancelled = false ORDER BY sle.postingDate ASC, sle.postingTime ASC")
    List<StockLedgerEntry> findByBatch(@Param("companyId") UUID companyId, @Param("batchId") UUID batchId);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.serialNumberId = :serialNumberId AND sle.isCancelled = false ORDER BY sle.postingDate ASC, sle.postingTime ASC")
    List<StockLedgerEntry> findBySerialNumber(@Param("companyId") UUID companyId, @Param("serialNumberId") UUID serialNumberId);

    @Query("SELECT MAX(sle.postingDate) FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.isCancelled = false")
    LocalDateTime getLastPostingDateTime(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT COUNT(sle) FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.isReposted = false")
    long countUnrepostedEntries(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.isCancelled = false ORDER BY sle.postingDate DESC, sle.postingTime DESC")
    List<StockLedgerEntry> findAllForValuationRecalc(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Modifying
    @Query("UPDATE StockLedgerEntry sle SET sle.isReposted = true WHERE sle.id IN :ids")
    int markAsReposted(@Param("ids") List<UUID> ids);

    @Query("SELECT sle FROM StockLedgerEntry sle WHERE sle.companyId = :companyId AND sle.itemId = :itemId AND sle.warehouseId = :warehouseId AND sle.valuationMethod = :method AND sle.isCancelled = false ORDER BY sle.postingDate ASC, sle.postingTime ASC")
    List<StockLedgerEntry> findByValuationMethod(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("method") Item.ValuationMethod method);
}