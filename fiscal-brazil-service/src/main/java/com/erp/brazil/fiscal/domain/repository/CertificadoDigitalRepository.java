package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Digital Certificate A1 entities.
 * PFX and password are stored encrypted with AES-256-GCM.
 */
@Repository
public interface CertificadoDigitalRepository extends JpaRepository<CertificadoDigitalA1, String> {

    Optional<CertificadoDigitalA1> findByCompanyIdAndCnpj(String companyId, String cnpj);

    List<CertificadoDigitalA1> findByCompanyId(String companyId);

    @Query("SELECT c FROM CertificadoDigitalA1 c WHERE c.status = :status")
    List<CertificadoDigitalA1> findByStatus(@Param("status") CertificadoDigitalA1.CertificateStatus status);

    @Query("SELECT c FROM CertificadoDigitalA1 c WHERE c.expiryDate <= :date AND c.status = 'ACTIVE'")
    List<CertificadoDigitalA1> findExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT c FROM CertificadoDigitalA1 c WHERE c.expiryDate < :date AND c.status != 'EXPIRED'")
    List<CertificadoDigitalA1> findExpiredBefore(@Param("date") LocalDate date);

    @Query("SELECT c FROM CertificadoDigitalA1 c WHERE c.expiryDate BETWEEN :start AND :end")
    List<CertificadoDigitalA1> findExpiringBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<CertificadoDigitalA1> findByCompanyIdAndStatus(String companyId, CertificadoDigitalA1.CertificateStatus status);

    @Query("SELECT c FROM CertificadoDigitalA1 c WHERE c.thumbprint = :thumbprint")
    Optional<CertificadoDigitalA1> findByThumbprint(@Param("thumbprint") String thumbprint);

    @Query("UPDATE CertificadoDigitalA1 c SET c.status = 'EXPIRED' WHERE c.expiryDate < :date AND c.status = 'ACTIVE'")
    int markExpiredCertificates(@Param("date") LocalDate date);

    @Query("UPDATE CertificadoDigitalA1 c SET c.status = 'EXPIRING_SOON' WHERE c.expiryDate BETWEEN :start AND :end AND c.status = 'ACTIVE'")
    int markExpiringSoonCertificates(@Param("start") LocalDate start, @Param("end") LocalDate end);

    Optional<CertificadoDigitalA1> findByIdAndCompanyId(String id, String companyId);

    boolean existsByCompanyIdAndCnpj(String companyId, String cnpj);
}