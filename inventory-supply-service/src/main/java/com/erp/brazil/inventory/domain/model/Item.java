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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
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

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public String getItemGroup() { return itemGroup; }
    public void setItemGroup(String itemGroup) { this.itemGroup = itemGroup; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public ValuationMethod getValuationMethod() { return valuationMethod; }
    public void setValuationMethod(ValuationMethod valuationMethod) { this.valuationMethod = valuationMethod; }

    public BigDecimal getStandardRate() { return standardRate; }
    public void setStandardRate(BigDecimal standardRate) { this.standardRate = standardRate; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsStockItem() { return isStockItem; }
    public void setIsStockItem(Boolean isStockItem) { this.isStockItem = isStockItem; }

    public Boolean getIsPurchasable() { return isPurchasable; }
    public void setIsPurchasable(Boolean isPurchasable) { this.isPurchasable = isPurchasable; }

    public Boolean getIsSellable() { return isSellable; }
    public void setIsSellable(Boolean isSellable) { this.isSellable = isSellable; }

    public Boolean getIsManufactured() { return isManufactured; }
    public void setIsManufactured(Boolean isManufactured) { this.isManufactured = isManufactured; }

    public Boolean getHasBatch() { return hasBatch; }
    public void setHasBatch(Boolean hasBatch) { this.hasBatch = hasBatch; }

    public Boolean getHasSerialNo() { return hasSerialNo; }
    public void setHasSerialNo(Boolean hasSerialNo) { this.hasSerialNo = hasSerialNo; }

    public String getBatchNumberSeries() { return batchNumberSeries; }
    public void setBatchNumberSeries(String batchNumberSeries) { this.batchNumberSeries = batchNumberSeries; }

    public String getSerialNumberSeries() { return serialNumberSeries; }
    public void setSerialNumberSeries(String serialNumberSeries) { this.serialNumberSeries = serialNumberSeries; }

    public UUID getDefaultWarehouseId() { return defaultWarehouseId; }
    public void setDefaultWarehouseId(UUID defaultWarehouseId) { this.defaultWarehouseId = defaultWarehouseId; }

    public BigDecimal getMinOrderQty() { return minOrderQty; }
    public void setMinOrderQty(BigDecimal minOrderQty) { this.minOrderQty = minOrderQty; }

    public BigDecimal getMaxOrderQty() { return maxOrderQty; }
    public void setMaxOrderQty(BigDecimal maxOrderQty) { this.maxOrderQty = maxOrderQty; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getReorderQty() { return reorderQty; }
    public void setReorderQty(BigDecimal reorderQty) { this.reorderQty = reorderQty; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }

    public String getHsnCode() { return hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }

    public String getNcmCode() { return ncmCode; }
    public void setNcmCode(String ncmCode) { this.ncmCode = ncmCode; }

    public String getCestCode() { return cestCode; }
    public void setCestCode(String cestCode) { this.cestCode = cestCode; }

    public String getOrigemMercadoria() { return origemMercadoria; }
    public void setOrigemMercadoria(String origemMercadoria) { this.origemMercadoria = origemMercadoria; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public enum ItemType {
        PRODUCT, SERVICE, RAW_MATERIAL, SUB_ASSEMBLY, FINISHED_GOOD, CONSUMABLE, FIXED_ASSET, OTHER
    }

    public enum ValuationMethod {
        FIFO, MOVING_AVERAGE, STANDARD
    }
}