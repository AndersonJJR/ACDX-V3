package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.ItemBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemBatchRepository extends JpaRepository<ItemBatch, UUID> {

    Optional<ItemBatch> findByCompanyIdAndItemIdAndBatchNo(UUID companyId, UUID itemId, String batchNo);

    List<ItemBatch> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    Page<ItemBatch> findByCompanyIdAndItemId(UUID companyId, UUID itemId, Pageable pageable);

    List<ItemBatch> findByCompanyIdAndItemIdAndIsActiveTrue(UUID companyId, UUID itemId);

    List<ItemBatch> findByCompanyIdAndItemVariantId(UUID companyId, UUID itemVariantId);

    List<ItemBatch> findByCompanyIdAndExpiryDateBefore(UUID companyId, LocalDate expiryDate);

    List<ItemBatch> findByCompanyIdAndExpiryDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<ItemBatch> findByCompanyIdAndWarehouseIdAndIsActiveTrue(UUID companyId, UUID warehouseId);

    @Query("SELECT ib FROM ItemBatch ib WHERE ib.companyId = :companyId AND ib.itemId = :itemId AND ib.isActive = true ORDER BY ib.expiryDate ASC NULLS LAST, ib.manufacturingDate ASC")
    List<ItemBatch> findActiveBatchesByItemForFifo(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId);

    @Query("SELECT SUM(ib.quantity) FROM ItemBatch ib WHERE ib.companyId = :companyId AND ib.itemId = :itemId AND ib.warehouseId = :warehouseId AND ib.isActive = true")
    java.math.BigDecimal getTotalQuantityByItemAndWarehouse(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId);

    @Modifying
    @Transactional
    @Query("UPDATE ItemBatch ib SET ib.quantity = ib.quantity + :delta WHERE ib.id = :batchId")
    int adjustQuantity(@Param("batchId") UUID batchId, @Param("delta") java.math.BigDecimal delta);

    @Modifying
    @Transactional
    @Query("UPDATE ItemBatch ib SET ib.valuationRate = :rate WHERE ib.id = :batchId")
    int updateValuationRate(@Param("batchId") UUID batchId, @Param("rate") java.math.BigDecimal rate);

    boolean existsByCompanyIdAndItemIdAndBatchNoAndIdNot(UUID companyId, UUID itemId, String batchNo, UUID id);

    @Query("SELECT ib FROM ItemBatch ib WHERE ib.companyId = :companyId AND ib.itemId = :itemId AND ib.batchNo = :batchNo AND ib.isActive = true")
    Optional<ItemBatch> findActiveByItemAndBatchNo(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("batchNo") String batchNo);
}