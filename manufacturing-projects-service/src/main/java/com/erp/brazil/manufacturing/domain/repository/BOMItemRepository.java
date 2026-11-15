package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.BOMItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BOMItemRepository extends JpaRepository<BOMItem, UUID> {

    List<BOMItem> findByBomIdOrderByPosition(UUID bomId);

    List<BOMItem> findByComponentId(UUID componentId);

    List<BOMItem> findByBomIdAndComponentId(UUID bomId, UUID componentId);

    @Query("SELECT bi FROM BOMItem bi WHERE bi.bom.id = :bomId AND bi.isPhantom = true")
    List<BOMItem> findPhantomItemsByBomId(@Param("bomId") UUID bomId);

    boolean existsByBomIdAndComponentId(UUID bomId, UUID componentId);
}