package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.ItemSerialNumber;
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
public interface ItemSerialNumberRepository extends JpaRepository<ItemSerialNumber, UUID> {

    Optional<ItemSerialNumber> findByCompanyIdAndItemIdAndSerialNo(UUID companyId, UUID itemId, String serialNo);

    List<ItemSerialNumber> findByCompanyIdAndItemId(UUID companyId, UUID itemId);

    Page<ItemSerialNumber> findByCompanyIdAndItemId(UUID companyId, UUID itemId, Pageable pageable);

    List<ItemSerialNumber> findByCompanyIdAndItemIdAndStatus(UUID companyId, UUID itemId, ItemSerialNumber.SerialStatus status);

    List<ItemSerialNumber> findByCompanyIdAndBatchId(UUID companyId, UUID batchId);

    List<ItemSerialNumber> findByCompanyIdAndWarehouseIdAndStatus(UUID companyId, UUID warehouseId, ItemSerialNumber.SerialStatus status);

    List<ItemSerialNumber> findByCompanyIdAndPurchaseReceiptId(UUID companyId, UUID purchaseReceiptId);

    List<ItemSerialNumber> findByCompanyIdAndSalesInvoiceId(UUID companyId, UUID salesInvoiceId);

    @Query("SELECT isn FROM ItemSerialNumber isn WHERE isn.companyId = :companyId AND isn.itemId = :itemId AND isn.status = :status ORDER BY isn.createdAt ASC")
    List<ItemSerialNumber> findAvailableSerialsByItem(@Param("companyId") UUID companyId, @Param("itemId") UUID itemId, @Param("status") ItemSerialNumber.SerialStatus status);

    boolean existsByCompanyIdAndItemIdAndSerialNoAndIdNot(UUID companyId, UUID itemId, String serialNo, UUID id);

    @Modifying
    @Query("UPDATE ItemSerialNumber isn SET isn.status = :status, isn.warehouseId = :warehouseId, isn.locationId = :locationId WHERE isn.id = :serialId")
    int updateStatusAndLocation(@Param("serialId") UUID serialId, @Param("status") ItemSerialNumber.SerialStatus status, @Param("warehouseId") UUID warehouseId, @Param("locationId") UUID locationId);
}