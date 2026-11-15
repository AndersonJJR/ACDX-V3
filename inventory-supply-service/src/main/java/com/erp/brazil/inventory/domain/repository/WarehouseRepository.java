package com.erp.brazil.inventory.domain.repository;

import com.erp.brazil.inventory.domain.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Optional<Warehouse> findByCompanyIdAndCode(UUID companyId, String code);

    Optional<Warehouse> findByCompanyIdAndIsDefaultTrue(UUID companyId);

    List<Warehouse> findByCompanyIdAndIsActiveTrue(UUID companyId);

    @Query("SELECT w FROM Warehouse w WHERE w.companyId = :companyId AND w.id = :id")
    Optional<Warehouse> findByCompanyIdAndId(@Param("companyId") UUID companyId, @Param("id") UUID id);

    boolean existsByCompanyIdAndCodeAndIdNot(UUID companyId, String code, UUID id);
}