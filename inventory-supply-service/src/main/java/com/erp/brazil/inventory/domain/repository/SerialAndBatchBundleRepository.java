package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.SerialAndBatchBundle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerialAndBatchBundleRepository extends JpaRepository<SerialAndBatchBundle, UUID> {

    List<SerialAndBatchBundle> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    Page<SerialAndBatchBundle> findByCompanyIdAndItemId(UUID companyId, UUID itemId, Pageable pageable);

    List<SerialAndBatchBundle> findByCompanyIdAndBatchId(UUID companyId, UUID batchId);

    List<SerialAndBatchBundle> findByCompanyIdAndSerialNumberId(UUID companyId, UUID serialNumberId);

    List<SerialAndBatchBundle> findByCompanyIdAndWarehouseId(UUID companyId, UUID warehouseId);

    List<SerialAndBatchBundle> findByCompanyIdAndVoucher(UUID companyId, String voucherType, String voucherNo);

    @Query("SELECT sbb FROM SerialAndBatchBundle sbb WHERE sbb.companyId = :companyId AND sbb.itemId = :itemId AND sbb.batchId = :batchId AND sbb.serialNumberId = :serialNumberId")
    Optional<SerialAndBatchBundle> findByItemBatchSerial(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("batchId") UUID batchId, @Param("serialNumberId") UUID serialNumberId);

    @Query("SELECT SUM(sbb.quantity) FROM SerialAndBatchBundle sbb WHERE sbb.companyId = :companyId AND sbb.itemId = :itemId AND sbb.batchId = :batchId AND sbb.serialNumberId = :serialNumberId")
    java.math.BigDecimal getQuantityByItemBatchSerial(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("batchId") UUID batchId, @Param("serialNumberId") UUID serialNumberId);
}