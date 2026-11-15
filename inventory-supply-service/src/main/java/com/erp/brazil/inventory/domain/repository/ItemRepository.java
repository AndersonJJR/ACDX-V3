package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.Item;
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
public interface ItemRepository extends JpaRepository<Item, UUID> {

    Optional<Item> findByCompanyIdAndCode(UUID companyId, String code);

    Optional<Item> findByCompanyIdAndName(UUID companyId, String name);

    List<Item> findByCompanyIdAndIsActiveTrue(UUID companyId);

    Page<Item> findByCompanyIdAndIsActiveTrue(UUID companyId, Pageable pageable);

    List<Item> findByCompanyIdAndItemType(UUID companyId, Item.ItemType itemType);

    List<Item> findByCompanyIdAndItemGroup(UUID companyId, String itemGroup);

    List<Item> findByCompanyIdAndIsStockItemTrue(UUID companyId);

    List<Item> findByCompanyIdAndReorderLevelLessThanEqualAndIsActiveTrue(UUID companyId, java.math.BigDecimal reorderLevel);

    @Query("SELECT i FROM Item i WHERE i.companyId = :companyId AND i.defaultWarehouseId = :warehouseId")
    List<Item> findByCompanyIdAndDefaultWarehouseId(@Param("companyId") UUID companyId, @Param("warehouseId") UUID warehouseId);

    @Query("SELECT i FROM Item i WHERE i.companyId = :companyId AND i.isStockItem = true AND i.reorderLevel IS NOT NULL AND i.reorderLevel > 0 AND (SELECT COALESCE(SUM(sle.stockQtyAfter), 0) FROM StockLedgerEntry sle WHERE sle.itemId = i.id AND sle.companyId = i.companyId ORDER BY sle.postingDate DESC) <= i.reorderLevel")
    List<Item> findItemsBelowReorderLevel(@Param("companyId") UUID companyId);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);

    @Query("SELECT COUNT(i) FROM Item i WHERE i.companyId = :companyId")
    long countByCompanyId(@Param("companyId") UUID companyId);
}