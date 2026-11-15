package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.SerialAndBatchBundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SerialAndBatchBundleRepository extends JpaRepository<SerialAndBatchBundle, UUID> {

    List<SerialAndBatchBundle> findByBatchId(UUID batchId);

    List<SerialAndBatchBundle> findBySerialNumberId(UUID serialNumberId);

    List<SerialAndBatchBundle> findByItemIdAndWarehouseId(UUID itemId, UUID warehouseId);

    @Query("SELECT sbb FROM SerialAndBatchBundle sbb WHERE sbb.itemId = :itemId AND sbb.warehouseId = :warehouseId AND sbb.batchId = :batchId")
    List<SerialAndBatchBundle> findByItemWarehouseAndBatch(@Param("itemId") UUID itemId, @Param("warehouseId") UUID warehouseId, @Param("batchId") UUID batchId);
}