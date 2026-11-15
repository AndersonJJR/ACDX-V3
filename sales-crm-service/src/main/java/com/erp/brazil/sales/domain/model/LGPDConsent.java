package com.erp.brazil.sales.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lgpd_consents")
public class LGPDConsent extends BaseEntity {

    @Column(name = "entity_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "purpose", length = 100, nullable = false)
    @Enumerated(EnumType.STRING)
    private LGPDConsentType purpose;

    @Column(name = "granted", nullable = false)
    private Boolean granted = false;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "consent_version", length = 20)
    private String consentVersion;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    public enum EntityType {
        CUSTOMER,
        CONTACT,
        LEAD
    }

    public boolean isActive() {
        return granted && revokedAt == null;
    }

    public void revoke() {
        this.granted = false;
        this.revokedAt = LocalDateTime.now();
    }

    public void grant() {
        this.granted = true;
        this.grantedAt = LocalDateTime.now();
        this.revokedAt = null;
    }
}