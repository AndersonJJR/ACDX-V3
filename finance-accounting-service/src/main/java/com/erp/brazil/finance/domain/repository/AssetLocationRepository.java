package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.AssetLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetLocationRepository extends JpaRepository<AssetLocation, UUID> {

    Optional<AssetLocation> findByCode(String code);

    List<AssetLocation> findByActiveTrueOrderByCode();

    Page<AssetLocation> findByActiveTrue(Pageable pageable);

    Page<AssetLocation> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);
}