package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_ledger_entries", indexes = {
    @Index(name = "idx_sle_item_warehouse_date", columnList = "item_id, warehouse_id, posting_date"),
    @Index(name = "idx_sle_voucher", columnList = "voucher_type, voucher_no"),
    @Index(name = "idx_sle_company_date", columnList = "company_id, posting_date"),
    @Index(name = "idx_sle_batch", columnList = "batch_id"),
    @Index(name = "idx_sle_serial", columnList = "serial_number_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class StockLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "item_variant_id")
    private UUID itemVariantId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "serial_number_id")
    private UUID serialNumberId;

    @Column(name = "qty_change", precision = 20, scale = 6, nullable = false)
    private BigDecimal qtyChange;

    @Column(name = "valuation_rate", precision = 20, scale = 6, nullable = false)
    private BigDecimal valuationRate;

    @Column(name = "stock_value", precision = 20, scale = 6, nullable = false)
    private BigDecimal stockValue;

    @Column(name = "stock_qty_after", precision = 20, scale = 6, nullable = false)
    private BigDecimal stockQtyAfter;

    @Column(name = "stock_value_after", precision = 20, scale = 6, nullable = false)
    private BigDecimal stockValueAfter;

    @Column(name = "voucher_type", nullable = false, length = 50)
    private String voucherType;

    @Column(name = "voucher_no", nullable = false, length = 100)
    private String voucherNo;

    @Column(name = "voucher_detail_no", length = 100)
    private String voucherDetailNo;

    @Column(name = "posting_date", nullable = false)
    private LocalDateTime postingDate;

    @Column(name = "posting_time", nullable = false)
    private LocalDateTime postingTime;

    @Column(name = "actual_qty", precision = 20, scale = 6)
    private BigDecimal actualQty;

    @Column(name = "incoming_rate", precision = 20, scale = 6)
    private BigDecimal incomingRate;

    @Column(name = "outgoing_rate", precision = 20, scale = 6)
    private BigDecimal outgoingRate;

    @Column(name = "valuation_method", length = 20)
    @Enumerated(EnumType.STRING)
    private Item.ValuationMethod valuationMethod;

    @Column(name = "is_cancelled", nullable = false)
    private Boolean isCancelled = false;

    @Column(name = "is_reposted", nullable = false)
    private Boolean isReposted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    // Partition key for database partitioning (company_id + posting_date)
    @Column(name = "partition_key", nullable = false, insertable = false, updatable = false)
    private String partitionKey;

    // Explicit getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public UUID getItemVariantId() { return itemVariantId; }
    public void setItemVariantId(UUID itemVariantId) { this.itemVariantId = itemVariantId; }

    public UUID getWarehouseId() { return warehouseId; }
    public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

    public UUID getLocationId() { return locationId; }
    public void setLocationId(UUID locationId) { this.locationId = locationId; }

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public UUID getSerialNumberId() { return serialNumberId; }
    public void setSerialNumberId(UUID serialNumberId) { this.serialNumberId = serialNumberId; }

    public BigDecimal getQtyChange() { return qtyChange; }
    public void setQtyChange(BigDecimal qtyChange) { this.qtyChange = qtyChange; }

    public BigDecimal getValuationRate() { return valuationRate; }
    public void setValuationRate(BigDecimal valuationRate) { this.valuationRate = valuationRate; }

    public BigDecimal getStockValue() { return stockValue; }
    public void setStockValue(BigDecimal stockValue) { this.stockValue = stockValue; }

    public BigDecimal getStockQtyAfter() { return stockQtyAfter; }
    public void setStockQtyAfter(BigDecimal stockQtyAfter) { this.stockQtyAfter = stockQtyAfter; }

    public BigDecimal getStockValueAfter() { return stockValueAfter; }
    public void setStockValueAfter(BigDecimal stockValueAfter) { this.stockValueAfter = stockValueAfter; }

    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }

    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }

    public String getVoucherDetailNo() { return voucherDetailNo; }
    public void setVoucherDetailNo(String voucherDetailNo) { this.voucherDetailNo = voucherDetailNo; }

    public LocalDateTime getPostingDate() { return postingDate; }
    public void setPostingDate(LocalDateTime postingDate) { this.postingDate = postingDate; }

    public LocalDateTime getPostingTime() { return postingTime; }
    public void setPostingTime(LocalDateTime postingTime) { this.postingTime = postingTime; }

    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }

    public BigDecimal getIncomingRate() { return incomingRate; }
    public void setIncomingRate(BigDecimal incomingRate) { this.incomingRate = incomingRate; }

    public BigDecimal getOutgoingRate() { return outgoingRate; }
    public void setOutgoingRate(BigDecimal outgoingRate) { this.outgoingRate = outgoingRate; }

    public Item.ValuationMethod getValuationMethod() { return valuationMethod; }
    public void setValuationMethod(Item.ValuationMethod valuationMethod) { this.valuationMethod = valuationMethod; }

    public Boolean getIsCancelled() { return isCancelled; }
    public void setIsCancelled(Boolean isCancelled) { this.isCancelled = isCancelled; }

    public Boolean getIsReposted() { return isReposted; }
    public void setIsReposted(Boolean isReposted) { this.isReposted = isReposted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getPartitionKey() { return partitionKey; }
    public void setPartitionKey(String partitionKey) { this.partitionKey = partitionKey; }
}