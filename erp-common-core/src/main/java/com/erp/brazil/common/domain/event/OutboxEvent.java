package com.erp.brazil.common.domain.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Outbox event for reliable event publishing using the Transactional Outbox pattern.
 * Events are stored in the same transaction as the business operation,
 * then published asynchronously by a separate process.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
        @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id"),
        @Index(name = "idx_outbox_tenant", columnList = "tenant_id")
})
public class OutboxEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "tenant_id", length = 100, nullable = false)
    private String tenantId;

    @Column(name = "event_type", length = 200, nullable = false)
    private String eventType;

    @Column(name = "aggregate_type", length = 100, nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100, nullable = false)
    private String aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "causation_id", length = 100)
    private String causationId;

    public OutboxEvent() {
        this.id = UUID.randomUUID().toString();
    }

    public static OutboxEvent create(String tenantId, DomainEvent domainEvent) {
        OutboxEvent event = new OutboxEvent();
        event.setTenantId(tenantId);
        event.setEventType(domainEvent.getEventType());
        event.setAggregateType(domainEvent.getAggregateType());
        event.setAggregateId(domainEvent.getAggregateId());
        event.setCorrelationId(domainEvent.getCorrelationId());
        event.setCausationId(domainEvent.getCausationId());
        // Payload will be set by the publisher with JSON serialization
        return event;
    }

    public enum OutboxStatus {
        PENDING,
        PUBLISHED,
        FAILED,
        DEAD_LETTER
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = OffsetDateTime.now();
    }

    public void markFailed(String error) {
        this.retryCount++;
        this.lastError = error;
        if (this.retryCount >= 5) {
            this.status = OutboxStatus.DEAD_LETTER;
        }
    }
}