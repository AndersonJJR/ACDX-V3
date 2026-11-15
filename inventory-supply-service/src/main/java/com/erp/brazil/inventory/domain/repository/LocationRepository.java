package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.Location;
import com.erp.brazil.inventory.domain.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    Optional<Location> findByCompanyIdAndWarehouseIdAndCode(UUID companyId, UUID warehouseId, String code);

    List<Location> findByCompanyIdAndWarehouseIdAndIsActiveTrue(UUID companyId, UUID warehouseId);

    List<Location> findByCompanyIdAndIsActiveTrue(UUID companyId);

    @Query("SELECT l FROM Location l WHERE l.companyId = :companyId AND l.warehouseId = :warehouseId AND l.locationType = :type AND l.isActive = true")
    List<Location> findByWarehouseAndType(@Param("companyId") UUID companyId, @Param("warehouseId") UUID warehouseId, @Param("type") Location.LocationType type);

    boolean existsByCompanyIdAndWarehouseIdAndCodeAndIdNot(UUID companyId, UUID warehouseId, String code, UUID id);
}