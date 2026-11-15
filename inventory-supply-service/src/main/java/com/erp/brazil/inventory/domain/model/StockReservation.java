package com.erp.brazil.inventory.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations", indexes = {
    @Index(name = "idx_reservation_item_warehouse", columnList = "item_id, warehouse_id"),
    @Index(name = "idx_reservation_voucher", columnList = "voucher_type, voucher_no"),
    @Index(name = "idx_reservation_status", columnList = "status"),
    @Index(name = "idx_reservation_expiry", columnList = "expiry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservation {

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

    @Column(name = "qty_reserved", precision = 20, scale = 6, nullable = false)
    private BigDecimal qtyReserved;

    @Column(name = "qty_delivered", precision = 20, scale = 6)
    private BigDecimal qtyDelivered = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "voucher_type", nullable = false, length = 50)
    private String voucherType;

    @Column(name = "voucher_no", nullable = false, length = 100)
    private String voucherNo;

    @Column(name = "voucher_detail_no", length = 100)
    private String voucherDetailNo;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "reference_doctype", length = 50)
    private String referenceDoctype;

    @Column(name = "reference_name", length = 100)
    private String referenceName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum ReservationStatus {
        ACTIVE, PARTIALLY_DELIVERED, DELIVERED, CANCELLED, EXPIRED
    }
}