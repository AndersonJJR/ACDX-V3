package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery_notes")
public class DeliveryNote extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "number", length = 20, unique = true, nullable = false)
    private String number;
    
    @Column(name = "series", length = 10)
    private String series;
    
    @Column(name = "sales_order_id", nullable = false)
    private UUID salesOrderId;
    
    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DeliveryNoteStatus status = DeliveryNoteStatus.DRAFT;
    
    @Column(name = "delivery_type", length = 20)
    private String deliveryType; // FULL, PARTIAL, BACKORDER
    
    @Column(name = "carrier", length = 100)
    private String carrier;
    
    @Column(name = "tracking_code", length = 100)
    private String trackingCode;
    
    @Column(name = "vehicle_plate", length = 10)
    private String vehiclePlate;
    
    @Column(name = "driver_name", length = 100)
    private String driverName;
    
    @Column(name = "driver_document", length = 20)
    private String driverDocument;
    
    @Column(name = "freight_value", precision = 15, scale = 2)
    private BigDecimal freightValue = BigDecimal.ZERO;
    
    @Column(name = "insurance_value", precision = 15, scale = 2)
    private BigDecimal insuranceValue = BigDecimal.ZERO;
    
    @Column(name = "total_weight", precision = 10, scale = 3)
    private BigDecimal totalWeight;
    
    @Column(name = "total_volume", precision = 10, scale = 3)
    private BigDecimal totalVolume;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "confirmed_by_id")
    private UUID confirmedById;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_by_id")
    private UUID cancelledById;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false, insertable = false, updatable = false)
    private SalesOrder salesOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    private UUID carrierId;
    
    @OneToMany(mappedBy = "deliveryNote", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<DeliveryNoteItem> items = new ArrayList<>();
    
    public boolean canBeConfirmed() {
        return status == DeliveryNoteStatus.DRAFT && !items.isEmpty();
    }
    
    public boolean isFullyDelivered() {
        return status == DeliveryNoteStatus.DELIVERED;
    }
}