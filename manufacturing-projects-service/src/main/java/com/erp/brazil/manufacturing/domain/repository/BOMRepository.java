package com.erp.brazil.manufacturing.domain.repository;

import com.erp.brazil.manufacturing.domain.model.BOM;
import com.erp.brazil.manufacturing.domain.model.BOM.BOMStatus;
import com.erp.brazil.manufacturing.domain.model.BOM.BOMType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BOMRepository extends JpaRepository<BOM, UUID> {

    Optional<BOM> findByProductIdAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID productId, BOMStatus status, LocalDateTime effectiveFrom, LocalDateTime effectiveTo);

    List<BOM> findByProductIdAndStatusOrderByVersionDesc(UUID productId, BOMStatus status);

    List<BOM> findByProductIdAndTypeAndStatus(UUID productId, BOMType type, BOMStatus status);

    Page<BOM> findByStatus(BOMStatus status, Pageable pageable);

    Page<BOM> findByProductId(UUID productId, Pageable pageable);

    List<BOM> findByIsPhantomTrueAndStatus(BOMStatus status);

    @Query(value = """
        WITH RECURSIVE bom_tree AS (
            SELECT b.id, b.product_id, b.type, b.status, b.version, b.base_quantity, b.scrap_percentage,
                   bi.id as item_id, bi.component_id, bi.quantity, bi.scrap_percentage as item_scrap,
                   bi.position, bi.unit_of_measure, bi.is_phantom, bi.component_type,
                   1 as level, CAST(b.id AS VARCHAR(1000)) as path
            FROM bom b
            JOIN bom_item bi ON bi.bom_id = b.id
            WHERE b.id = :bomId
            UNION ALL
            SELECT b.id, b.product_id, b.type, b.status, b.version, b.base_quantity, b.scrap_percentage,
                   bi.id as item_id, bi.component_id, bi.quantity, bi.scrap_percentage as item_scrap,
                   bi.position, bi.unit_of_measure, bi.is_phantom, bi.component_type,
                   bt.level + 1, CONCAT(bt.path, '->', CAST(b.id AS VARCHAR(1000)))
            FROM bom_tree bt
            JOIN bom_item bi ON bi.component_id = bt.product_id
            JOIN bom b ON b.product_id = bi.component_id AND b.status = 'ACTIVE' AND b.is_phantom = true
            WHERE bt.level < 10 AND bt.path NOT LIKE CONCAT('%->', CAST(b.id AS VARCHAR(1000)), '%')
        )
        SELECT * FROM bom_tree ORDER BY level, position
        """, nativeQuery = true)
    List<BOMExplosionResult> explodeBOM(@Param("bomId") UUID bomId);

    interface BOMExplosionResult {
        UUID getId();
        UUID getProductId();
        String getType();
        String getStatus();
        String getVersion();
        java.math.BigDecimal getBaseQuantity();
        java.math.BigDecimal getScrapPercentage();
        UUID getItemId();
        UUID getComponentId();
        java.math.BigDecimal getQuantity();
        java.math.BigDecimal getItemScrap();
        Integer getPosition();
        String getUnitOfMeasure();
        Boolean getIsPhantom();
        String getComponentType();
        Integer getLevel();
        String getPath();
    }

    boolean existsByProductIdAndVersion(UUID productId, String version);

    long countByProductId(UUID productId);
}