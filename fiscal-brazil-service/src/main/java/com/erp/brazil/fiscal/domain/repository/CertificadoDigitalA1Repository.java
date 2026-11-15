package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;
import com.erp.brazil.fiscal.domain.model.CertificateStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificadoDigitalA1Repository extends MongoRepository<CertificadoDigitalA1, String> {
    Optional<CertificadoDigitalA1> findByCompanyCnpjAndAlias(String companyCnpj, String alias);

    List<CertificadoDigitalA1> findByCompanyCnpj(String companyCnpj);

    List<CertificadoDigitalA1> findByCompanyCnpjAndStatus(String companyCnpj, CertificateStatus status);

    List<CertificadoDigitalA1> findByStatus(CertificateStatus status);

    List<CertificadoDigitalA1> findByNotAfterBefore(LocalDate date);

    List<CertificadoDigitalA1> findByNotAfterBetween(LocalDate start, LocalDate end);

    boolean existsByCompanyCnpjAndAlias(String companyCnpj, String alias);

    void deleteByCompanyCnpj(String companyCnpj);
}