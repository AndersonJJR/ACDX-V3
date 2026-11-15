package com.erp.brazil.fiscal.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_certificado_digital_a1")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDigitalA1 {
    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "company_cnpj", length = 14, nullable = false)
    private String companyCnpj;

    @Column(name = "company_name", length = 115, nullable = false)
    private String companyName;

    @Column(name = "alias", length = 100, nullable = false)
    private String alias;

    @Column(name = "subject_dn", length = 500)
    private String subjectDn;

    @Column(name = "issuer_dn", length = 500)
    private String issuerDn;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "not_before")
    private LocalDate notBefore;

    @Column(name = "not_after")
    private LocalDate notAfter;

    @Column(name = "encrypted_pkcs12", columnDefinition = "TEXT")
    private String encryptedPkcs12;

    @Column(name = "encryption_key_id", length = 100)
    private String encryptionKeyId;

    @Column(name = "password_hint", length = 200)
    private String passwordHint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CertificateStatus status = CertificateStatus.ACTIVE;

    @Column(name = "environment", length = 1)
    private String environment = "1"; // 1=Production, 2=Homologation

    @Column(name = "last_renewal_alert_sent")
    private LocalDateTime lastRenewalAlertSent;

    @Column(name = "alert_60_days_sent")
    private Boolean alert60DaysSent = false;

    @Column(name = "alert_30_days_sent")
    private Boolean alert30DaysSent = false;

    @Column(name = "alert_7_days_sent")
    private Boolean alert7DaysSent = false;

    @Column(name = "alert_1_day_sent")
    private Boolean alert1DaySent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revocation_reason", length = 255)
    private String revocationReason;

    public int getDaysToExpiration() {
        if (notAfter == null) return -1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), notAfter);
    }

    public boolean isExpiringSoon(int days) {
        return getDaysToExpiration() >= 0 && getDaysToExpiration() <= days;
    }

    public boolean isExpired() {
        return getDaysToExpiration() < 0;
    }
}