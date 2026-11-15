package com.erp.brazil.hr.domain.event;

import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {

    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    private Instant occurredAt = Instant.now();
    private String aggregateId;
    private Long companyId;
    private String triggeredBy;
    private Map<String, Object> metadata;

    public abstract String getEventName();
}