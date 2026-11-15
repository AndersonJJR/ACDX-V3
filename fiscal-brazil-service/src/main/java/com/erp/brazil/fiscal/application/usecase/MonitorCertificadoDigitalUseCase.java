package com.erp.brazil.fiscal.application.usecase;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.model.CertificateStatus;
import com.erp.brazil.fiscal.domain.repository.CertificadoDigitalRepository;
import com.erp.brazil.fiscal.infrastructure.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorCertificadoDigitalUseCase {

    private final CertificadoDigitalRepository certificadoRepository;
    private final EventPublisher eventPublisher;

    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @Transactional
    public void checkExpiringCertificates() {
        log.info("Checking expiring digital certificates...");

        LocalDate today = LocalDate.now();
        LocalDate alert60 = today.plusDays(60);
        LocalDate alert30 = today.plusDays(30);
        LocalDate alert7 = today.plusDays(7);
        LocalDate alert1 = today.plusDays(1);

        // Check 60 days
        processAlerts(certificadoRepository.findExpiringBetween(today, alert60), 60);

        // Check 30 days
        processAlerts(certificadoRepository.findExpiringBetween(today, alert30), 30);

        // Check 7 days
        processAlerts(certificadoRepository.findExpiringBetween(today, alert7), 7);

        // Check 1 day
        processAlerts(certificadoRepository.findExpiringBetween(today, alert1), 1);

        // Check expired
        List<CertificadoDigitalA1> expired = certificadoRepository.findExpiredBefore(today);
        for (CertificadoDigitalA1 cert : expired) {
            if (cert.status() != CertificateStatus.EXPIRED) {
                // Note: CertificadoDigitalA1 is a record, so we need to create a new instance
                // In a real implementation, this would use a repository update method
                log.warn("Certificate expired: {} - CNPJ: {}", cert.alias(), cert.cnpj());
            }
        }
    }

    private void processAlerts(List<CertificadoDigitalA1> certificados, int days) {
        for (CertificadoDigitalA1 cert : certificados) {
            boolean alreadySent = switch (days) {
                case 60 -> cert.alert60DaysSent() != null && cert.alert60DaysSent();
                case 30 -> cert.alert30DaysSent() != null && cert.alert30DaysSent();
                case 7 -> cert.alert7DaysSent() != null && cert.alert7DaysSent();
                case 1 -> cert.alert1DaySent() != null && cert.alert1DaySent();
                default -> false;
            };

            if (!alreadySent && cert.status() == CertificateStatus.ACTIVE) {
                sendAlert(cert, days);
                markAlertSent(cert, days);
                // certificadoPort.save(cert); // Would need update method
            }
        }
    }

    private void sendAlert(CertificadoDigitalA1 cert, int days) {
        log.warn("Certificate expiring in {} days: {} - CNPJ: {} - Expires: {}",
                days, cert.alias(), cert.cnpj(), cert.notAfter());
    }

    private void markAlertSent(CertificadoDigitalA1 cert, int days) {
        // In a real implementation, this would update the certificate
        if (days <= 30) {
            // cert.setStatus(CertificateStatus.PENDING_RENEWAL);
        }
    }

    public List<CertificadoDigitalA1> findByCompanyCnpj(String companyCnpj) {
        return certificadoRepository.findByCompanyCnpj(companyCnpj);
    }

    public List<CertificadoDigitalA1> findByCompanyCnpjAndStatus(String companyCnpj, CertificateStatus status) {
        return certificadoRepository.findByCompanyIdAndStatus(companyCnpj, status);
    }

    public List<CertificadoDigitalA1> findExpiringSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(days);
        return certificadoRepository.findExpiringBetween(today, maxDate);
    }

    public List<CertificadoDigitalA1> findExpired() {
        return certificadoRepository.findExpiredBefore(LocalDate.now());
    }

    public Optional<CertificadoDigitalA1> findById(String id) {
        return certificadoRepository.findById(id);
    }
}