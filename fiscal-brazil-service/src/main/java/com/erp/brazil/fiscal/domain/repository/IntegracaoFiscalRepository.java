package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.IntegracaoFiscal;
import com.erp.brazil.fiscal.domain.model.IntegracaoStatus;
import com.erp.brazil.fiscal.domain.model.IntegracaoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegracaoFiscalRepository extends JpaRepository<IntegracaoFiscal, String> {
    List<IntegracaoFiscal> findByCompanyCnpj(String companyCnpj);

    List<IntegracaoFiscal> findByCompanyCnpjAndTipo(String companyCnpj, IntegracaoTipo tipo);

    List<IntegracaoFiscal> findByCompanyCnpjAndStatus(String companyCnpj, IntegracaoStatus status);

    Optional<IntegracaoFiscal> findByCompanyCnpjAndTipoAndIsDefaultTrue(String companyCnpj, IntegracaoTipo tipo);

    List<IntegracaoFiscal> findByTipoAndStatus(IntegracaoTipo tipo, IntegracaoStatus status);

    boolean existsByCompanyCnpjAndTipoAndProvedor(String companyCnpj, IntegracaoTipo tipo, String provedor);
}