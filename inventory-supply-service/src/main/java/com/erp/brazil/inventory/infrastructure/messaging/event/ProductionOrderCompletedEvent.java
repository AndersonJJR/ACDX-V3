package com.erp.brazil.inventory.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ProductionOrderCompletedEvent {

    private UUID productionOrderId;
    private UUID companyId;
    private List<ItemDetail> items;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime timestamp;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class ItemDetail {
        private UUID itemId;
        private UUID itemVariantId;
        private UUID warehouseId;
        private java.math.BigDecimal qty;
        private java.math.BigDecimal valuationRate;
        private UUID batchId;

        // Explicit getters and setters
        public UUID getItemId() { return itemId; }
        public void setItemId(UUID itemId) { this.itemId = itemId; }

        public UUID getItemVariantId() { return itemVariantId; }
        public void setItemVariantId(UUID itemVariantId) { this.itemVariantId = itemVariantId; }

        public UUID getWarehouseId() { return warehouseId; }
        public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }

        public java.math.BigDecimal getQty() { return qty; }
        public void setQty(java.math.BigDecimal qty) { this.qty = qty; }

        public java.math.BigDecimal getValuationRate() { return valuationRate; }
        public void setValuationRate(java.math.BigDecimal valuationRate) { this.valuationRate = valuationRate; }

        public UUID getBatchId() { return batchId; }
        public void setBatchId(UUID batchId) { this.batchId = batchId; }
    }

    // Explicit getters and setters
    public UUID getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(UUID productionOrderId) { this.productionOrderId = productionOrderId; }

    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }

    public List<ItemDetail> getItems() { return items; }
    public void setItems(List<ItemDetail> items) { this.items = items; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}