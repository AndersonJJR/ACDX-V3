package com.erp.brazil.fiscal.application.port;

import com.erp.brazil.fiscal.domain.model.CertificadoDigitalA1;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CertificadoDigitalPort {
    CertificadoDigitalA1 save(CertificadoDigitalA1 certificado);

    Optional<CertificadoDigitalA1> findById(String id);

    Optional<CertificadoDigitalA1> findByCompanyCnpjAndAlias(String companyCnpj, String alias);

    List<CertificadoDigitalA1> findByCompanyCnpj(String companyCnpj);

    List<CertificadoDigitalA1> findByCompanyCnpjAndStatus(String companyCnpj, com.erp.brazil.fiscal.domain.model.CertificadoStatus status);

    List<CertificadoDigitalA1> findByStatus(com.erp.brazil.fiscal.domain.model.CertificadoStatus status);

    List<CertificadoDigitalA1> findByNotAfterBefore(LocalDate date);

    List<CertificadoDigitalA1> findByNotAfterBetween(LocalDate start, LocalDate end);

    boolean existsByCompanyCnpjAndAlias(String companyCnpj, String alias);

    void deleteByCompanyCnpj(String companyCnpj);
}