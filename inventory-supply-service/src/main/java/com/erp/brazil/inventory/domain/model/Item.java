package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_company_code", columnList = "company_id, code", unique = true),
    @Index(name = "idx_item_company_name", columnList = "company_id, name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "item_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "item_group", length = 50)
    private String itemGroup;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @Column(name = "valuation_method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ValuationMethod valuationMethod;

    @Column(name = "standard_rate", precision = 20, scale = 6)
    private BigDecimal standardRate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_stock_item", nullable = false)
    private Boolean isStockItem = true;

    @Column(name = "is_purchasable", nullable = false)
    private Boolean isPurchasable = true;

    @Column(name = "is_sellable", nullable = false)
    private Boolean isSellable = true;

    @Column(name = "is_manufactured", nullable = false)
    private Boolean isManufactured = false;

    @Column(name = "has_batch", nullable = false)
    private Boolean hasBatch = false;

    @Column(name = "has_serial_no", nullable = false)
    private Boolean hasSerialNo = false;

    @Column(name = "batch_number_series", length = 50)
    private String batchNumberSeries;

    @Column(name = "serial_number_series", length = 50)
    private String serialNumberSeries;

    @Column(name = "default_warehouse_id")
    private UUID defaultWarehouseId;

    @Column(name = "min_order_qty", precision = 20, scale = 6)
    private BigDecimal minOrderQty;

    @Column(name = "max_order_qty", precision = 20, scale = 6)
    private BigDecimal maxOrderQty;

    @Column(name = "reorder_level", precision = 20, scale = 6)
    private BigDecimal reorderLevel;

    @Column(name = "reorder_qty", precision = 20, scale = 6)
    private BigDecimal reorderQty;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "ncm_code", length = 20)
    private String ncmCode;

    @Column(name = "cest_code", length = 10)
    private String cestCode;

    @Column(name = "origem_mercadoria", length = 1)
    private String origemMercadoria;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum ItemType {
        PRODUCT, SERVICE, RAW_MATERIAL, SUB_ASSEMBLY, FINISHED_GOOD, CONSUMABLE, FIXED_ASSET, OTHER
    }

    public enum ValuationMethod {
        FIFO, MOVING_AVERAGE, STANDARD
    }
}