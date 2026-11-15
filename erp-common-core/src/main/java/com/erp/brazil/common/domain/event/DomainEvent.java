package com.erp.brazil.common.domain.event;

import com.erp.brazil.common.domain.model.TenantContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base class for all domain events.
 * Contains common metadata for event sourcing and audit trail.
 */
@Getter
@Setter
@ToString
public abstract class DomainEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String eventId;
    private final String eventType;
    private final OffsetDateTime occurredAt;
    private String tenantId;
    private String userId;
    private String companyId;
    private String correlationId;
    private String causationId;
    private int version = 1;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.occurredAt = OffsetDateTime.now();
        this.tenantId = TenantContext.getTenantId();
        this.userId = TenantContext.getUserId();
        this.companyId = TenantContext.getCompanyId();
    }

    protected DomainEvent(String correlationId, String causationId) {
        this();
        this.correlationId = correlationId;
        this.causationId = causationId;
    }

    /**
     * Gets the aggregate ID that this event belongs to.
     * Must be implemented by subclasses.
     *
     * @return the aggregate identifier
     */
    public abstract String getAggregateId();

    /**
     * Gets the aggregate type (entity name).
     *
     * @return the aggregate type
     */
    public abstract String getAggregateType();
}