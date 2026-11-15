package com.erp.brazil.fiscal.domain.repository;

import com.erp.brazil.fiscal.domain.model.RetencaoFonte;
import com.erp.brazil.fiscal.domain.model.RetencaoFonte.RetencaoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RetencaoFonteRepository extends JpaRepository<RetencaoFonte, String> {
    List<RetencaoFonte> findByCompanyCnpjAndDataPagamentoBetween(String companyCnpj, LocalDate start, LocalDate end);

    List<RetencaoFonte> findByCompanyCnpjAndTipoAndDataPagamentoBetween(String companyCnpj, RetencaoType tipo, LocalDate start, LocalDate end);

    List<RetencaoFonte> findByFornecedorCnpjAndDataPagamentoBetween(String fornecedorCnpj, LocalDate start, LocalDate end);

    @Query("SELECT SUM(r.valorRetido) FROM RetencaoFonte r WHERE r.companyCnpj = :companyCnpj AND r.tipo = :tipo AND r.dataPagamento BETWEEN :start AND :end")
    BigDecimal sumValorRetidoByCompanyAndTipoAndPeriod(@Param("companyCnpj") String companyCnpj, @Param("tipo") RetencaoType tipo, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT r.tipo, SUM(r.valorRetido) FROM RetencaoFonte r WHERE r.companyCnpj = :companyCnpj AND r.dataPagamento BETWEEN :start AND :end GROUP BY r.tipo")
    List<Object[]> sumValorRetidoByTipoAndPeriod(@Param("companyCnpj") String companyCnpj, @Param("start") LocalDate start, @Param("end") LocalDate end);
}