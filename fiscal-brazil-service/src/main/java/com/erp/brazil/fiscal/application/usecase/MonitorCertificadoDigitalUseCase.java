package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.application.dto.EventDTO;
import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.repository.CertificadoDigitalRepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled use case for monitoring digital certificate expiry.
 * Runs daily, checks certificates expiring in 30, 15, 7, 3, 1 days.
 */
@Service
@Transactional
public class MonitorCertificadoDigitalUseCase {

    private final CertificadoDigitalRepository certificadoRepository;
    private final EventPublisher eventPublisher;

    // Alert thresholds in days
    private static final int[] ALERT_DAYS = {30, 15, 7, 3, 1, 0};

    public MonitorCertificadoDigitalUseCase(CertificadoDigitalRepository certificadoRepository,
                                             EventPublisher eventPublisher) {
        this.certificadoRepository = certificadoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Runs daily at 8:00 AM to check certificate expiries.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void execute() {
        LocalDate today = LocalDate.now();
        LocalDate maxAlertDate = today.plusDays(30);

        // Find certificates expiring within 30 days
        List<CertificadoDigitalA1> expiringCerts = certificadoRepository.findExpiringBetween(today, maxAlertDate);

        for (CertificadoDigitalA1 cert : expiringCerts) {
            int daysUntilExpiry = cert.daysUntilExpiry();

            // Check if this is an alert day
            boolean shouldAlert = false;
            for (int alertDay : ALERT_DAYS) {
                if (daysUntilExpiry == alertDay) {
                    shouldAlert = true;
                    break;
                }
            }

            if (shouldAlert) {
                publishAlert(cert, daysUntilExpiry);
            }

            // Update certificate status
            updateCertificateStatus(cert, daysUntilExpiry);
        }

        // Mark expired certificates
        List<CertificadoDigitalA1> expiredCerts = certificadoRepository.findExpiredBefore(today);
        for (CertificadoDigitalA1 cert : expiredCerts) {
            if (!"EXPIRED".equals(cert.status().name())) {
                certificadoRepository.save(new CertificadoDigitalA1(
                        cert.id(), cert.companyId(), cert.alias(), cert.pfxEncrypted(),
                        cert.passwordEncrypted(), cert.cnpj(), cert.companyName(),
                        cert.expiryDate(), CertificadoDigitalA1.CertificateStatus.EXPIRED,
                        cert.serialNumber(), cert.issuer(), cert.subject(), cert.thumbprint(),
                        cert.createdAt(), LocalDateTime.now(), cert.lastUsedAt(), cert.renewalAlertDays()
                ));

                publishAlert(cert, cert.daysUntilExpiry());
            }
        }
    }

    /**
     * Manual trigger for certificate check.
     */
    public void checkNow() {
        execute();
    }

    /**
     * Get all certificates with their alert status for a company.
     */
    @Transactional(readOnly = true)
    public List<CertificadoDigitalA1> getCertificatesWithAlerts(String companyId) {
        return certificadoRepository.findByCompanyId(companyId);
    }

    private void publishAlert(CertificadoDigitalA1 cert, int daysUntilExpiry) {
        CertificadoDigitalA1.CertificateStatus alertStatus;
        if (daysUntilExpiry < 0) {
            alertStatus = CertificadoDigitalA1.CertificateStatus.EXPIRED;
        } else if (daysUntilExpiry <= 7) {
            alertStatus = CertificadoDigitalA1.CertificateStatus.EXPIRING_SOON;
        } else {
            alertStatus = cert.status();
        }

        var event = new EventDTO.CertificadoExpiryAlertEvent(
                UUID.randomUUID().toString(),
                cert.id(),
                cert.companyId(),
                cert.alias(),
                cert.cnpj(),
                cert.companyName(),
                cert.expiryDate().atStartOfDay(),
                daysUntilExpiry,
                alertStatus,
                LocalDateTime.now()
        );
        eventPublisher.publish(event);
    }

    private void updateCertificateStatus(CertificadoDigitalA1 cert, int daysUntilExpiry) {
        CertificadoDigitalA1.CertificateStatus newStatus;
        if (daysUntilExpiry < 0) {
            newStatus = CertificadoDigitalA1.CertificateStatus.EXPIRED;
        } else if (daysUntilExpiry <= cert.renewalAlertDays()) {
            newStatus = CertificadoDigitalA1.CertificateStatus.EXPIRING_SOON;
        } else {
            newStatus = CertificadoDigitalA1.CertificateStatus.ACTIVE;
        }

        if (newStatus != cert.status()) {
            certificadoRepository.save(new CertificadoDigitalA1(
                    cert.id(), cert.companyId(), cert.alias(), cert.pfxEncrypted(),
                    cert.passwordEncrypted(), cert.cnpj(), cert.companyName(),
                    cert.expiryDate(), newStatus,
                    cert.serialNumber(), cert.issuer(), cert.subject(), cert.thumbprint(),
                    cert.createdAt(), LocalDateTime.now(), cert.lastUsedAt(), cert.renewalAlertDays()
            ));
        }
    }
}