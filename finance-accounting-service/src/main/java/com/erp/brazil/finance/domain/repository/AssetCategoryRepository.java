package com.erp.brazil.finance.domain.repository;

import com.erp.brazil.finance.domain.model.AssetCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {

    Optional<AssetCategory> findByCode(String code);

    List<AssetCategory> findByActiveTrueOrderByCode();

    Page<AssetCategory> findByActiveTrue(Pageable pageable);

    Page<AssetCategory> findAll(Pageable pageable);

    boolean existsByCodeAndIdNot(String code, UUID id);
}