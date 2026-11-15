package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.Location;
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
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByWarehouseIdAndCode(UUID warehouseId, String code);

    List<Location> findByWarehouseId(UUID warehouseId);

    Page<Location> findByWarehouseId(UUID warehouseId, Pageable pageable);

    List<Location> findByWarehouseIdAndIsActiveTrue(UUID warehouseId);

    List<Location> findByWarehouseIdAndLocationType(UUID warehouseId, Location.LocationType locationType);

    List<Location> findByWarehouseIdAndIsPickableTrue(UUID warehouseId);

    List<Location> findByWarehouseIdAndIsReceivableTrue(UUID warehouseId);

    boolean existsByWarehouseIdAndCodeAndIdNot(UUID warehouseId, String code, UUID id);

    @Query("SELECT l FROM Location l WHERE l.warehouseId = :warehouseId AND l.isActive = true AND l.isPickable = true ORDER BY l.aisle, l.rack, l.shelf, l.bin")
    List<Location> findPickableLocationsOrdered(@Param("warehouseId") UUID warehouseId);
}