package com.erp.brazil.hr.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "audit_logs")
@CompoundIndex(name = "idx_entity_id_type", def = "{'entityId': 1, 'entityType': 1}")
@CompoundIndex(name = "idx_timestamp", def = "{'timestamp': -1}", expireAfterSeconds = 2555 * 24 * 60 * 60)
@CompoundIndex(name = "idx_company_timestamp", def = "{'companyId': 1, 'timestamp': -1}")
@CompoundIndex(name = "idx_user_timestamp", def = "{'userId': 1, 'timestamp': -1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    @Field("entity_id")
    @Indexed
    private String entityId;

    @Field("entity_type")
    @Indexed
    private String entityType;

    @Field("company_id")
    @Indexed
    private Long companyId;

    @Field("user_id")
    @Indexed
    private String userId;

    @Field("action")
    private String action; // CREATE, UPDATE, DELETE, VIEW, EXPORT, LOGIN, LOGOUT, etc.

    @Field("timestamp")
    private Instant timestamp = Instant.now();

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("request_id")
    private String requestId;

    @Field("session_id")
    private String sessionId;

    @Field("old_values")
    private Map<String, Object> oldValues;

    @Field("new_values")
    private Map<String, Object> newValues;

    @Field("changed_fields")
    private java.util.List<String> changedFields;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("risk_level")
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Field("compliance_tags")
    private java.util.List<String> complianceTags;

    @Field("is_masked")
    private Boolean isMasked = false;

    @Field("masked_fields")
    private java.util.List<String> maskedFields;

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder().id(UUID.randomUUID().toString());
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}